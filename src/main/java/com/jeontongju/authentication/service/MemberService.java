package com.jeontongju.authentication.service;

import com.jeontongju.authentication.domain.Member;
import com.jeontongju.authentication.domain.SnsAccount;
import com.jeontongju.authentication.dto.MailInfoDto;
import com.jeontongju.authentication.dto.request.*;
import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import com.jeontongju.authentication.dto.response.JwtTokenResponse;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.dto.response.oauth.google.GoogleOAuthInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoOAuthInfo;
import com.jeontongju.authentication.dto.temp.ConsumerInfoForCreateBySnsRequestDto;
import com.jeontongju.authentication.dto.temp.MemberEmailForKeyDto;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.enums.SnsTypeEnum;
import com.jeontongju.authentication.exception.*;
import com.jeontongju.authentication.mapper.MemberMapper;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.repository.SnsAccountRepository;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.service.feign.seller.SellerClientService;
import com.jeontongju.authentication.utils.Auth19Manager;
import com.jeontongju.authentication.utils.CustomErrMessage;
import com.jeontongju.authentication.utils.MailManager;
import com.jeontongju.authentication.utils.OAuth2Manager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.crypto.SecretKey;
import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final SnsAccountRepository snsAccountRepository;
  private final ConsumerClientService consumerClientService;
  private final SellerClientService sellerClientService;
  private final MemberMapper memberMapper;
  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private final Auth19Manager auth19Manager;

  @Value("${jwt.secret}")
  private String secret;

  public MailAuthCodeResponseDto sendEmailAuthForFind(EmailInfoForAuthRequestDto authRequestDto)
      throws MessagingException, UnsupportedEncodingException {

    MemberRoleEnum memberRoleEnum =
        authRequestDto.getMemberRole().equals("ROLE_CONSUMER")
            ? MemberRoleEnum.ROLE_CONSUMER
            : MemberRoleEnum.ROLE_SELLER;

    memberRepository
        .findByUsernameAndMemberRoleEnum(authRequestDto.getEmail(), memberRoleEnum)
        .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    MailInfoDto mailInfoDto = MailManager.sendAuthEmail(authRequestDto.getEmail());
    return MailAuthCodeResponseDto.builder().authCode(mailInfoDto.getValidCode()).build();
  }

  public MailAuthCodeResponseDto sendEmailAuthForSignUp(EmailInfoForAuthRequestDto authRequestDto)
      throws MessagingException, UnsupportedEncodingException {

    // 이메일 + 역할 중복 체크
    if (isUniqueKeyDuplicated(authRequestDto.getEmail(), authRequestDto.getMemberRole())) {
      throw new DuplicateEmailException(CustomErrMessage.EMAIL_ALREADY_IN_USE);
    }

    MailInfoDto mailInfoDto = MailManager.sendAuthEmail(authRequestDto.getEmail());

    return MailAuthCodeResponseDto.builder().authCode(mailInfoDto.getValidCode()).build();
  }

  /**
   * 회원 가입 (소비자)
   *
   * @param signupRequestDto 회원가입 시 필요한 정보 (이메일, 비밀번호, imp_uid)
   * @throws JSONException
   * @throws IOException
   */
  @Transactional
  public void signupForConsumer(ConsumerInfoForSignUpRequestDto signupRequestDto)
      throws JSONException, IOException {

    ImpAuthInfo impAuthInfo = auth19Manager.authenticate19(signupRequestDto.getImpUid());

    Member savedConsumer;
    if (signupRequestDto.getIsMerge()) { // 계정 통합 시

      savedConsumer =
          memberRepository
              .findByUsernameAndMemberRoleEnum(
                  signupRequestDto.getEmail(), MemberRoleEnum.ROLE_CONSUMER)
              .orElseThrow(() -> new MemberNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
      savedConsumer.assignPassword(signupRequestDto.getPassword());

      consumerClientService.updateConsumerForAccountConsolidation(
          memberMapper.toAccountConsolidationDto(savedConsumer.getMemberId(), impAuthInfo));
    } else {

      savedConsumer =
          memberRepository.save(
              memberMapper.toEntity(
                  signupRequestDto.getEmail(),
                  signupRequestDto.getPassword(),
                  MemberRoleEnum.ROLE_CONSUMER));

      // 소비자 테이블에 성인 인증으로 얻어온 정보 저장
      consumerClientService.createConsumerForSignup(
          memberMapper.toConsumerCreateDto(
              savedConsumer.getMemberId(), savedConsumer.getUsername(), impAuthInfo));
    }
  }

  @Transactional
  public void signupForSeller(SellerInfoForSignUpRequestDto signUpRequestDto)
      throws JSONException, IOException {

    // 성인 인증
    ImpAuthInfo impAuthInfo = auth19Manager.authenticate19(signUpRequestDto.getImpUid());

    Member savedSeller =
        memberRepository.save(
            memberMapper.toEntity(
                signUpRequestDto.getEmail(),
                signUpRequestDto.getPassword(),
                MemberRoleEnum.ROLE_SELLER));

    sellerClientService.createSellerForSignup(
        memberMapper.toSellerCreateDto(savedSeller.getMemberId(), signUpRequestDto, impAuthInfo));
  }

  private Boolean isUniqueKeyDuplicated(String email, String memberRole) {

    Member foundMember = memberRepository.findByUsername(email).orElse(null);
    return foundMember != null && foundMember.getMemberRoleEnum().name().equals(memberRole);
  }

  @Transactional
  public void signInForConsumerByKakao(String code) throws DuplicateEmailException {

    KakaoOAuthInfo kakaoOAuthInfo = OAuth2Manager.authenticateByKakao(code);
    String email = kakaoOAuthInfo.getKakao_account().getEmail();
    if (isUniqueKeyDuplicated(email, MemberRoleEnum.ROLE_CONSUMER.name())) {
      throw new DuplicateEmailException(CustomErrMessage.EMAIL_ALREADY_IN_USE);
    }

    Member savedMember =
        memberRepository.save(memberMapper.toEntity(email, "", MemberRoleEnum.ROLE_CONSUMER));
    snsAccountRepository.save(
        SnsAccount.register(
            SnsTypeEnum.KAKAO.name() + "_" + kakaoOAuthInfo.getId(),
            SnsTypeEnum.KAKAO.name(),
            savedMember));

    consumerClientService.createConsumerForSignupBySns(
        ConsumerInfoForCreateBySnsRequestDto.toDto(
            savedMember.getMemberId(),
            email,
            kakaoOAuthInfo.getKakao_account().getProfile().getProfile_image_url()));
  }

  @Transactional
  public void signInForConsumerByGoogle(String code) {
    GoogleOAuthInfo googleOAuthInfo = OAuth2Manager.authenticateByGoogle(code);
    String email = googleOAuthInfo.getEmail();
    if (isUniqueKeyDuplicated(email, MemberRoleEnum.ROLE_CONSUMER.name())) {
      throw new DuplicateEmailException(CustomErrMessage.EMAIL_ALREADY_IN_USE);
    }

    Member savedMember =
        memberRepository.save(memberMapper.toEntity(email, "", MemberRoleEnum.ROLE_CONSUMER));
    snsAccountRepository.save(
        SnsAccount.register(
            SnsTypeEnum.GOOGLE.name() + "_" + googleOAuthInfo.getId(),
            SnsTypeEnum.GOOGLE.name(),
            savedMember));

    consumerClientService.createConsumerForSignupBySns(
        ConsumerInfoForCreateBySnsRequestDto.toDto(
            savedMember.getMemberId(), email, googleOAuthInfo.getPicture()));
  }

  @Transactional
  public JwtTokenResponse renewAccessTokenByRefreshToken(String refreshToken) {

    log.info("MemberService's renewAccessTokenByRefreshToken executes..");
    try {
      log.info("redisTemplate starts..");
      ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();

      byte[] keyBytes = Decoders.BASE64.decode(secret);
      SecretKey key = Keys.hmacShaKeyFor(keyBytes);

      Claims claims = checkValid(refreshToken, key);
      String memberId = claims.get("memberId", String.class);
      Member member =
          memberRepository
              .findByMemberId(Long.parseLong(memberId))
              .orElseThrow(
                  () ->
                      new MalformedRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN));
      String refreshKey = member.getMemberRoleEnum().name() + "_" + member.getUsername();
      log.info("redisTemplate.opsForValue get..");
      String refreshTokenInRedis = stringStringValueOperations.get(refreshKey);
      log.info("redisTemplate Successful end!");

      // refreshtoken이 탈취되었을 가능성이 있을지 확인
      if (!refreshToken.equals(refreshTokenInRedis)) {
        // 다르면 탈취된 것으로 판단
        log.info("[refreshToken is different in redis]!!");
        log.info("[delete refresh key & value in redis]..");
        redisTemplate.delete(refreshKey);
        throw new MalformedRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN);
      }

      // Refresh Token Rotation 전략, access token 과 함께 refresh token 갱신
      String renewedAccessToken = jwtTokenProvider.recreateToken(member);
      String renewedRefreshToken = jwtTokenProvider.createRefreshToken(Long.parseLong(memberId));
      stringStringValueOperations.set(refreshKey, renewedRefreshToken);

      log.info("access token & refresh token renewed.");
      return JwtTokenResponse.builder()
          .accessToken("Bearer " + renewedAccessToken)
          .refreshToken(renewedRefreshToken)
          .build();

    } catch (ExpiredJwtException e) {
      log.info("refresh token expired.");
      throw new ExpiredRefreshTokenException(CustomErrMessage.EXPIRED_REFRESH_TOKEN);
    } catch (IllegalArgumentException | SignatureException | MalformedJwtException e) {
      log.info("wrong refresh token.");
      throw new NotValidRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN);
    } catch (Exception e) {
      log.info("unforeseen error!!");
      throw new UnforeseenException(CustomErrMessage.UNFORESEEM_ERROR);
    }
  }

  private Claims checkValid(String jwt, SecretKey key)
      throws IllegalArgumentException,
          ExpiredJwtException,
          SignatureException,
          MalformedJwtException {

    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
  }

  public void confirmOriginPassword(Long memberId, PasswordForCheckRequestDto checkRequestDto) {

    Member foundMember =
        memberRepository
            .findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));

    String passwordInDB = foundMember.getPassword();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    if (!passwordEncoder.matches(checkRequestDto.getOriginalPassword(), passwordInDB)) {
      throw new NotCorrespondPassword(CustomErrMessage.NOT_CORRESPOND_ORIGIN_PASSWORD);
    }
  }

  @Transactional
  public void modifyPassword(PasswordForChangeRequestDto changeRequestDto) {

    Member foundMember =
        memberRepository
            .findByUsernameAndMemberRoleEnum(
                changeRequestDto.getEmail(), changeRequestDto.getMemberRole())
            .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    foundMember.assignPassword(changeRequestDto.getNewPassword());
  }

  @Transactional
  public void modifyPasswordForSimpleChange(
      Long memberId, PasswordForSimpleChangeRequestDto simpleChangeRequestDto) {

    Member foundMember =
        memberRepository
            .findByMemberId(memberId)
            .orElseThrow(() -> new ConsumerNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    foundMember.assignPassword(simpleChangeRequestDto.getNewPassword());
  }

  public MemberEmailForKeyDto getMemberEmailForKey(Long memberId) {

    Member foundMember =
        memberRepository
            .findByMemberId(memberId)
            .orElseThrow(() -> new MemberNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    return MemberEmailForKeyDto.builder().email(foundMember.getUsername()).build();
  }

  /**
   * 회원 탈퇴 처리
   *
   * @param memberId 탈퇴 처리할 회원 식별자
   */
  @Transactional
  public void withdraw(Long memberId) {

    Member foundMember =
        memberRepository
            .findByMemberId(memberId)
            .orElseThrow(() -> new MemberNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    foundMember.delete();
  }

  /**
   * 최초 소셜 로그인 후, 성인인증 처리
   *
   * @param memberId 로그인 한 회원 식별자
   */
  public void authentication19AfterSnsSignIn(
      Long memberId, ImpUidForAdultCertificationRequestDto adultCertificationRequestDto)
      throws JSONException, IOException {

    ImpAuthInfo impAuthInfo =
        auth19Manager.authenticate19(adultCertificationRequestDto.getImpUid());
    consumerClientService.updateConsumerByAuth19(
        memberMapper.toImpAuthInfoDto(memberId, impAuthInfo));
  }
}
