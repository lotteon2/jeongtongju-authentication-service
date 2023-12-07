package com.jeontongju.authentication.service.feign.consumer;

import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateByKakaoRequestDto;
import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumerClientService {

  private final ConsumerServiceClient consumerServiceClient;

  public void createConsumerForSignup(ConsumerInfoForCreateRequestDto createRequestDto) {
    consumerServiceClient.createConsumerForSignup(createRequestDto);
  }

  public void createConsumerForSignupByKakao(
      ConsumerInfoForCreateByKakaoRequestDto createByKakaoRequestDto) {
    consumerServiceClient.createConsumerForSignupByKakao(createByKakaoRequestDto);
  }
}
