package com.jeontongju.authentication.service;

import com.jeontongju.authentication.domain.Member;
import com.jeontongju.authentication.dto.MailInfoDto;
import com.jeontongju.authentication.dto.request.*;
import com.jeontongju.authentication.dto.response.*;
import com.jeontongju.authentication.dto.temp.MemberEmailForKeyDto;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.exception.*;
import com.jeontongju.authentication.feign.auction.AuctionClientService;
import com.jeontongju.authentication.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.feign.seller.SellerClientService;
import com.jeontongju.authentication.mapper.MemberMapper;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.repository.SnsAccountRepository;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.utils.Auth19Manager;
import com.jeontongju.authentication.utils.CustomErrMessage;
import com.jeontongju.authentication.utils.MailManager;
import io.github.bitbox.bitbox.dto.AgeDistributionForShowResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
  private final AuctionClientService auctionClientService;
  private final MemberMapper memberMapper;
  private final RedisTemplate<String, String> redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private final Auth19Manager auth19Manager;

  @Value("${jwt.secret}")
  private String secret;

  /**
   * 비밀번호 찾기 시, 인증을 위한 이메일 전송
   *
   * @param authRequestDto 이메일 + 역할 정보(UNIQUE)
   * @return {MailAuthCodeResponseDto} 이메일로 전송된 유효코드(8자)
   * @throws MessagingException
   * @throws UnsupportedEncodingException
   */
  public MailAuthCodeResponseDto sendEmailAuthForFind(EmailInfoForAuthRequestDto authRequestDto)
      throws MessagingException, UnsupportedEncodingException {

    MemberRoleEnum memberRoleEnum =
        authRequestDto.getMemberRole().equals("ROLE_CONSUMER")
            ? MemberRoleEnum.ROLE_CONSUMER
            : MemberRoleEnum.ROLE_SELLER;

    memberRepository
        .findByUsernameAndMemberRoleEnum(authRequestDto.getEmail(), memberRoleEnum)
        .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    MailInfoDto mailInfoDto =
        MailManager.sendAuthEmail(authRequestDto.getEmail(), "비밀번호 찾기 인증 유효코드입니다.");
    return MailAuthCodeResponseDto.builder().authCode(mailInfoDto.getValidCode()).build();
  }

  /**
   * 비밀번호 찾기 시, 변경 처리
   *
   * @param changeRequestDto 비밀번호 변경을 위해 필요한 정보 (UNIQUE KEY + 새로운 비밀번호)
   */
  @Transactional
  public void modifyPassword(PasswordForChangeRequestDto changeRequestDto) {

    Member foundMember =
        memberRepository
            .findByUsernameAndMemberRoleEnum(
                changeRequestDto.getEmail(), changeRequestDto.getMemberRole())
            .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
    foundMember.assignPassword(changeRequestDto.getNewPassword());
  }

  /**
   * 기존 비밀번호 확인
   *
   * @param memberId 로그인 한 회원 식별자
   * @param checkRequestDto 사용자가 입력한 비밀번호
   */
  public void confirmOriginPassword(Long memberId, PasswordForCheckRequestDto checkRequestDto) {

    Member foundMember = getMember(memberId);

    String passwordInDB = foundMember.getPassword();
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    if (!passwordEncoder.matches(checkRequestDto.getOriginalPassword(), passwordInDB)) {
      throw new NotCorrespondPassword(CustomErrMessage.NOT_CORRESPOND_ORIGIN_PASSWORD);
    }
  }

  /**
   * 비밀번호 단순 변경 시, 변경 처리
   *
   * @param memberId 로그인 한 회원 식별자
   * @param simpleChangeRequestDto 변경할 새로운 비밀번호
   */
  @Transactional
  public void modifyPasswordForSimpleChange(
      Long memberId, PasswordForSimpleChangeRequestDto simpleChangeRequestDto) {

    Member foundMember = getMember(memberId);
    foundMember.assignPassword(simpleChangeRequestDto.getNewPassword());
  }

  /**
   * 회원 가입 시, 중복회원 확인 및 인증을 위한 유효코드 생성 및 메일 발송
   *
   * @param authRequestDto 이메일 + 역할 정보(UNIQUE)
   * @return {MailAuthCodeResponseDto} 이메일로 전송된 유효코드(8자)
   * @throws MessagingException
   * @throws UnsupportedEncodingException
   */
  public MailAuthCodeResponseDto sendEmailAuthForSignUp(EmailInfoForAuthRequestDto authRequestDto)
      throws MessagingException, UnsupportedEncodingException {

    Member foundMember =
        getMemberByUniqueKey(authRequestDto.getEmail(), authRequestDto.getMemberRole());
    boolean isSocial = false;
    boolean isUniqueKeyDuplicated = foundMember != null;

    // 이메일 + 역할 중복 체크
    if (isUniqueKeyDuplicated) {

      if (foundMember.getSnsAccountList().isEmpty()) {
        throw new DuplicateEmailException(CustomErrMessage.EMAIL_ALREADY_IN_USE);
      }
      isSocial = true;
    }

    MailInfoDto mailInfoDto =
        MailManager.sendAuthEmail(authRequestDto.getEmail(), "회원가입 인증 유효코드입니다.");

    return MailAuthCodeResponseDto.builder()
        .authCode(mailInfoDto.getValidCode())
        .isSocial(isSocial)
        .build();
  }

  /**
   * 회원 가입 (소비자)
   *
   * @param signupRequestDto 소비자 회원 가입 시 필요한 정보 (이메일, 비밀번호, imp_uid)
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

  /**
   * 회원 가입 (셀러)
   *
   * @param signUpRequestDto 셀러 회원 가입 시 필요한 정보
   * @throws JSONException
   * @throws IOException
   */
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

  /**
   * 소셜 로그인으로만 계정이 존재하는지에 대한 여부
   *
   * @param memberId 확인할 회원의 식별자
   * @return {Boolean} 해당 회원 소셜 계정(만) 존재 여부
   */
  public Boolean isExistSocialAccount(Long memberId) {

    Member foundMember = getMember(memberId);

    // 소셜 계정이 존재하고 계정 통합이 되지 않았을 경우
    if (!foundMember.getSnsAccountList().isEmpty()) {
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      if (passwordEncoder.matches("", foundMember.getPassword())) {
        return true;
      }
    }
    return false;
  }

  /**
   * REFRESH TOKEN 검증을 통한 ACCESS TOKEN 재발급
   *
   * @param refreshToken 클라이언트에서 넘겨 받은 REFRESH TOKEN
   * @return {JwtTokenResponse} 재발급된 토큰 정보
   */
  @Transactional
  public JwtTokenResponse renewAccessTokenByRefreshToken(String refreshToken) {

    log.info("[MemberService's renewAccessTokenByRefreshToken executes]");
    try {
      log.info("[redisTemplate starts]");
      ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();

      byte[] keyBytes = Decoders.BASE64.decode(secret);
      SecretKey key = Keys.hmacShaKeyFor(keyBytes);

      refreshToken = refreshToken.replace("Bearer ", "");
      Claims claims = checkValid(refreshToken, key);
      String memberId = claims.get("memberId", String.class);
      log.info("[memberId]: " + memberId);
      Member member =
          memberRepository
              .findByMemberId(Long.parseLong(memberId))
              .orElseThrow(() -> new MemberNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
      String refreshKey = member.getMemberRoleEnum().name() + "_" + member.getUsername();
      log.info("[redisTemplate.opsForValue get]");
      String refreshTokenInRedis = stringStringValueOperations.get(refreshKey);
      log.info("[redisTemplate Successful end]!");

      // refreshtoken이 탈취되었을 가능성이 있을지 확인
      if (!refreshToken.equals(refreshTokenInRedis)) {
        // 다르면 탈취된 것으로 판단
        log.info("[refreshToken is different in redis]!!");
        redisTemplate.delete(refreshKey);
        log.info("[delete refresh key & value in redis]");
        throw new MalformedRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN);
      }

      // Refresh Token Rotation 전략, access token 과 함께 refresh token 갱신
      String renewedAccessToken = jwtTokenProvider.recreateToken(member);
      String renewedRefreshToken = jwtTokenProvider.createRefreshToken(Long.parseLong(memberId));
      stringStringValueOperations.set(refreshKey, renewedRefreshToken);

      log.info("[Access token & Refresh token renewed]");
      return JwtTokenResponse.builder()
          .accessToken("Bearer " + renewedAccessToken)
          .refreshToken("Bearer " + renewedRefreshToken)
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

  /**
   * REFRESH TOKEN 유효성 검사
   *
   * @param jwt REFRESH TOKEN
   * @param key 시크릿 키
   * @return {Claims} 토큰에 포함된 정보
   */
  private Claims checkValid(String jwt, SecretKey key)
      throws IllegalArgumentException,
          ExpiredJwtException,
          SignatureException,
          MalformedJwtException {

    log.info("[MemberService's checkValid executes]");
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
  }

  public MemberEmailForKeyDto getMemberEmailForKey(Long memberId) {

    Member foundMember = getMember(memberId);
    return MemberEmailForKeyDto.builder().email(foundMember.getUsername()).build();
  }

  /**
   * 회원 탈퇴 처리
   *
   * @param memberId 탈퇴 처리할 회원 식별자
   */
  @Transactional
  public void withdraw(Long memberId) {

    Member foundMember = getMember(memberId);
    consumerClientService.delete(memberId);
    foundMember.delete();
  }

  /**
   * 회원 탈퇴 처리(셀러)
   *
   * @param sellerId 탈퇴 처리할 회원(셀러) 식별자
   */
  public void deleteSeller(Long sellerId) {

    Member foundMember = getMember(sellerId);
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

  /**
   * 관리자의 서비스 현황 조회(입점 대기, 신규 셀러 및 소비자, 탈퇴 회원, 경매 대기)
   *
   * @param memberRole 해당 작업을 호출할 회원의 역할(ROLE_ADMIN)
   * @return {SiteSituationForAdminManagingResponseDto} 서비스의 현황 정보
   */
  @Transactional
  public SiteSituationForAdminManagingResponseDto getSiteSituation(MemberRoleEnum memberRole) {

    if (memberRole != MemberRoleEnum.ROLE_ADMIN) {
      throw new NotAdminAccessDeniedException(CustomErrMessage.N0T_ADMIN_ACCESS_DENIED);
    }

    Long countOfApprovalWaitingSeller = sellerClientService.getCountOfApprovalWaitingSeller();
    Long countOfApprovalWaitingAuctionProduct =
        auctionClientService.getCountOfApprovalWaitingProduct();
    LocalDate currentDate = LocalDate.now();
    LocalDateTime currentDateStartOfDay = currentDate.atStartOfDay();

    // 신규 유저(오늘)
    List<Member> registerConsumerAtToday =
        memberRepository.findByMemberRoleEnumAndIsDeletedAndCreatedAtAfter(
            MemberRoleEnum.ROLE_CONSUMER, false, currentDateStartOfDay);
    // 신규 셀러(오늘, 미승인 포함)
    List<Member> registerSellerAtToday =
        memberRepository.findByMemberRoleEnumAndIsDeletedAndCreatedAtAfter(
            MemberRoleEnum.ROLE_SELLER, false, currentDateStartOfDay);
    // 탈퇴 회원(오늘)
    List<Member> deletedMemberAtToday =
        memberRepository.findByIsDeletedAndCreatedAtAfter(true, currentDateStartOfDay);
    return memberMapper.toSiteSituationDto(
        countOfApprovalWaitingSeller,
        registerSellerAtToday.size(),
        registerConsumerAtToday.size(),
        deletedMemberAtToday.size(),
        countOfApprovalWaitingAuctionProduct);
  }

  /**
   * 모든 회원 현황 조회 (관리자)
   *
   * @param memberRole 해당 작업을 호출할 회원의 역할(ROLE_ADMIN)
   * @return {MemberInfoForAdminManagingResponseDto} 모든 회원 현황 정보
   */
  public MemberInfoForAdminManagingResponseDto getMembersResult(MemberRoleEnum memberRole) {

    if (memberRole != MemberRoleEnum.ROLE_ADMIN) {
      throw new NotAdminAccessDeniedException(CustomErrMessage.N0T_ADMIN_ACCESS_DENIED);
    }

    AgeDistributionForShowResponseDto ageDistributionForAllMembers =
        consumerClientService.getAgeDistributionForAllMembers();

    LocalDate today = LocalDate.now();
    LocalDate aWeekAgo = today.minusDays(6L);

    List<Object[]> memberCountsProgress =
        getMemberCountsByCreatedAtAndMemberRoleEnumFromAWeekAgo(
            aWeekAgo, MemberRoleEnum.ROLE_CONSUMER);

    Map<LocalDate, Long> consumers = new LinkedHashMap<>();
    Map<LocalDate, Long> sellers = new LinkedHashMap<>();

    long daysDifference = ChronoUnit.DAYS.between(aWeekAgo, today);

    for (int i = 0; i <= daysDifference; i++) {

      LocalDate curDate = aWeekAgo.plusDays(i);
      log.info("[curDate]: " + curDate);

      consumers.put(curDate, 0L);
      sellers.put(curDate, 0L);
    }

    for (Object[] memberCounts : memberCountsProgress) {
      Date date = (Date) memberCounts[0];
      Long memberCount = (Long) memberCounts[1];
      log.info("[date]: " + date);
      log.info("[memberCount]: " + memberCount);
      consumers.put(date.toLocalDate(), memberCount);
    }

    List<Object[]> sellerCountsProgress =
        getMemberCountsByCreatedAtAndMemberRoleEnumFromAWeekAgo(
            aWeekAgo, MemberRoleEnum.ROLE_SELLER);

    for (Object[] sellerCounts : sellerCountsProgress) {
      Date date = (Date) sellerCounts[0];
      Long sellerCount = (Long) sellerCounts[1];

      sellers.put(date.toLocalDate(), sellerCount);
    }
    return memberMapper.toMemberInfoForAdminDto(ageDistributionForAllMembers, consumers, sellers);
  }

  /**
   * 일주일 전부터 현재까지 요일별 신규 회원 수 가져오기 (소비자 or 셀러)
   *
   * @param aWeekAgo 일주일 전 날짜 정보
   * @param memberRoleEnum 찾을 신규 회원 역할
   * @return {List<Object[]>} 요일별 신규 회원 수, size: 7
   */
  private List<Object[]> getMemberCountsByCreatedAtAndMemberRoleEnumFromAWeekAgo(
      LocalDate aWeekAgo, MemberRoleEnum memberRoleEnum) {

    return memberRepository.getMemberCountsByCreatedAtAndMemberRoleEnumFromAWeekAgo(
        aWeekAgo.atStartOfDay(), memberRoleEnum);
  }

  public Member getMemberByUniqueKey(String email, String memberRole) {

    MemberRoleEnum memberRoleEnum =
        memberRole.equals("ROLE_CONSUMER")
            ? MemberRoleEnum.ROLE_CONSUMER
            : MemberRoleEnum.ROLE_SELLER;

    return memberRepository.findByUsernameAndMemberRoleEnum(email, memberRoleEnum).orElse(null);
  }

  /**
   * memberId로 해당 Member객체 찾기(공통화)
   *
   * @param memberId 회원 식별자
   * @return {Member} 찾은 회원 객체
   */
  private Member getMember(Long memberId) {
    return memberRepository
        .findByMemberId(memberId)
        .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
  }
}
