package com.jeontongju.authentication.service;

import com.jeontongju.authentication.dto.ConsumerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.ConsumerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.ImpAuthInfoResponse;
import com.jeontongju.authentication.dto.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import com.jeontongju.authentication.service.feign.seller.SellerClientService;
import com.jeontongju.authentication.utils.Auth19Manager;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final ConsumerClientService consumerClientService;
  private final SellerClientService sellerClientService;

  @Value("${store.imp.key}")
  private String impKey;

  @Value("${store.imp.secret}")
  private String impSecret;

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
}
