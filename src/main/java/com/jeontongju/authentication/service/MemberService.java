package com.jeontongju.authentication.service;

import com.jeontongju.authentication.dto.MailInfoDto;
import com.jeontongju.authentication.dto.request.*;
import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoOAuthInfo;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.entity.SnsAccount;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.enums.SnsTypeEnum;
import com.jeontongju.authentication.exception.DuplicateEmailException;
import com.jeontongju.authentication.mapper.MemberMapper;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.repository.SnsAccountRepository;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.service.feign.seller.SellerClientService;
import com.jeontongju.authentication.utils.Auth19Manager;
import com.jeontongju.authentication.utils.CustomErrMessage;
import com.jeontongju.authentication.utils.MailManager;
import com.jeontongju.authentication.utils.OAuth2Manager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final SnsAccountRepository snsAccountRepository;
  private final ConsumerClientService consumerClientService;
  private final SellerClientService sellerClientService;
  private final MemberMapper memberMapper;

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

    consumerClientService.createConsumerForSignupByKakao(
        ConsumerInfoForCreateByKakaoRequestDto.toDto(
            savedMember.getMemberId(),
            email,
            kakaoOAuthInfo.getKakao_account().getProfile().getProfile_image_url()));
  }
}
