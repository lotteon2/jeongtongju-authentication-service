package com.jeontongju.authentication.service;

import com.jeontongju.authentication.dto.MailInfoDto;
import com.jeontongju.authentication.dto.request.*;
import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import com.jeontongju.authentication.dto.response.JwtTokenResponse;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.dto.response.oauth.google.GoogleOAuthInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoOAuthInfo;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.entity.SnsAccount;
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
import java.util.Optional;
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

    @Transactional
    public void signupForConsumer(ConsumerInfoForSignUpRequestDto signupRequestDto)
            throws JSONException, IOException {

        ImpAuthInfo impAuthInfo = Auth19Manager.authenticate19(signupRequestDto.getImpUid());

        Member savedConsumer =
                memberRepository.save(
                        memberMapper.toEntity(
                                signupRequestDto.getEmail(),
                                signupRequestDto.getPassword(),
                                MemberRoleEnum.ROLE_CONSUMER));

        consumerClientService.createConsumerForSignup(
                ConsumerInfoForCreateRequestDto.toDto(
                        savedConsumer.getMemberId(), savedConsumer.getUsername(), impAuthInfo));
    }

    @Transactional
    public void signupForSeller(SellerInfoForSignUpRequestDto signUpRequestDto)
            throws JSONException, IOException {

        // 성인 인증
        ImpAuthInfo impAuthInfo = Auth19Manager.authenticate19(signUpRequestDto.getImpUid());

        Member savedSeller =
                memberRepository.save(
                        memberMapper.toEntity(
                                signUpRequestDto.getEmail(),
                                signUpRequestDto.getPassword(),
                                MemberRoleEnum.ROLE_SELLER));

        sellerClientService.createSellerForSignup(
                SellerInfoForCreateRequestDto.toDto(
                        savedSeller.getMemberId(), signUpRequestDto, impAuthInfo));
    }

    private Boolean isUniqueKeyDuplicated(String email, String memberRole) {

        Member foundMember = memberRepository.findByUsername(email).orElse(null);
        return foundMember != null && foundMember.getMemberRoleEnum().toString().equals(memberRole);
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
                ConsumerInfoForCreateByKakaoRequestDto.toDto(
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
                ConsumerInfoForCreateByKakaoRequestDto.toDto(
                        savedMember.getMemberId(), email, googleOAuthInfo.getPicture()));
    }

    public JwtTokenResponse renewAccessTokenByRefreshToken(String refreshToken) {

        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        try {
            Claims claims = checkValid(refreshToken, key);
            String memberId = claims.get("memberId", String.class);
            Member member =
                    memberRepository
                            .findByMemberId(Long.parseLong(memberId))
                            .orElseThrow(
                                    () ->
                                            new MalformedRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN));
            String refreshKey = member.getMemberRoleEnum().name() + "_" + member.getUsername();
            String refreshTokenInRedis = stringStringValueOperations.get(refreshKey);

            // refreshtoken이 탈취되었을 가능성이 있을지 확인
            if (!refreshToken.equals(refreshTokenInRedis)) {
                // 다르면 탈취된 것으로 판단
                redisTemplate.delete(refreshKey);
                throw new MalformedRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN);
            }

            // Refresh Token Rotation 전략, access token 과 함께 refresh token 갱신
            String renewedAccessToken = jwtTokenProvider.recreateToken(member);
            String renewedRefreshToken = jwtTokenProvider.createRefreshToken(Long.parseLong(memberId));
            stringStringValueOperations.set(refreshKey, renewedRefreshToken);

            return JwtTokenResponse.builder()
                    .accessToken(renewedAccessToken)
                    .refreshToken(renewedRefreshToken)
                    .build();

        } catch (ExpiredJwtException e) {
            throw new ExpiredRefreshTokenException(CustomErrMessage.EXPIRED_REFRESH_TOKEN);
        } catch (IllegalArgumentException | SignatureException | MalformedJwtException e) {
            throw new NotValidRefreshTokenException(CustomErrMessage.MALFORMED_REFRESH_TOKEN);
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
                        .findByUsernameAndMemberRoleEnum(changeRequestDto.getEmail(), changeRequestDto.getMemberRole())
                        .orElseThrow(() -> new EntityNotFoundException(CustomErrMessage.NOT_FOUND_MEMBER));
        foundMember.assignPassword(changeRequestDto.getNewPassword());
    }
}
