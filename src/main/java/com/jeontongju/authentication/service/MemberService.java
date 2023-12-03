package com.jeontongju.authentication.service;

import com.jeontongju.authentication.dto.ConsumerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.ConsumerInfoForSignUpForRequestDto;
import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.repository.MemberRepository;
import com.jeontongju.authentication.service.feign.consumer.ConsumerClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final ConsumerClientService consumerClientService;

  @Transactional
  public void signupForConsumer(ConsumerInfoForSignUpForRequestDto signupRequestDto) {
    Member savedConsumer =
        memberRepository.save(
            Member.register(signupRequestDto.getEmail(), signupRequestDto.getPassword()));

    consumerClientService.createConsumerForSignup(
        ConsumerInfoForCreateRequestDto.toDto(
            savedConsumer.getMemberId(), savedConsumer.getUsername(), signupRequestDto.getName()));
  }
}
