package com.jeontongju.authentication.service;

import com.jeontongju.authentication.dto.MailInfoDto;
import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.request.ConsumerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.request.EmailInfoForAuthRequestDto;
import com.jeontongju.authentication.dto.request.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.request.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.response.ImpAuthInfoResponse;
import com.jeontongju.authentication.dto.response.MailAuthCodeResponseDto;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.exception.DuplicateEmailException;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.service.feign.seller.SellerClientService;
import com.jeontongju.authentication.utils.Auth19Manager;
import com.jeontongju.authentication.utils.CustomErrMessage;
import com.jeontongju.authentication.utils.MailManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final ConsumerClientService consumerClientService;
  private final SellerClientService sellerClientService;
  private final JavaMailSender mailSender;

  @Value("${store.imp.key}")
  private String impKey;

  @Value("${store.imp.secret}")
  private String impSecret;

  @Value("${store.email.from}")
  private String from;

  public MailAuthCodeResponseDto sendEmailAuthForSignUp(EmailInfoForAuthRequestDto authRequestDto)
      throws MessagingException, UnsupportedEncodingException {

    // 이메일 + 역할 중복 체크
    if (isUniqueKeyDuplicated(authRequestDto.getEmail(), authRequestDto.getMemberRole())) {
      throw new DuplicateEmailException(CustomErrMessage.EMAIL_ALREADY_IN_USE);
    }

    MailInfoDto mailInfoDto =
        MailManager.sendAuthEmail(mailSender, from, authRequestDto.getEmail());

    return MailAuthCodeResponseDto.builder().authCode(mailInfoDto.getValidCode()).build();
  }

  @Transactional
  public void signupForConsumer(ConsumerInfoForSignUpRequestDto signupRequestDto) {
    Member savedConsumer =
        memberRepository.save(
            Member.register(
                signupRequestDto.getEmail(),
                signupRequestDto.getPassword(),
                MemberRoleEnum.ROLE_CONSUMER));

    consumerClientService.createConsumerForSignup(
        ConsumerInfoForCreateRequestDto.toDto(
            savedConsumer.getMemberId(), savedConsumer.getUsername(), signupRequestDto.getName()));
  }

  @Transactional
  public void signupForSeller(SellerInfoForSignUpRequestDto signUpRequestDto) {

    try {
      // 성인 인증
      ImpAuthInfoResponse impAuthInfoResponse =
          Auth19Manager.authenticate19(signUpRequestDto.getImpUid(), impKey, impSecret);

      Member savedSeller =
          memberRepository.save(
              Member.register(
                  signUpRequestDto.getEmail(),
                  signUpRequestDto.getPassword(),
                  MemberRoleEnum.ROLE_SELLER));

      sellerClientService.createSellerForSignup(
          SellerInfoForCreateRequestDto.toDto(
              savedSeller.getMemberId(), signUpRequestDto, impAuthInfoResponse));

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private Boolean isUniqueKeyDuplicated(String email, String memberRole) {

    Member foundMember = memberRepository.findByUsername(email).orElse(null);
    return foundMember != null && foundMember.getMemberRoleEnum().toString().equals(memberRole);
  }
}
