package com.jeontongju.authentication.service.feign.consumer;

import com.jeontongju.authentication.dto.ConsumerInfoForCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumerClientService {

  private final ConsumerServiceClient consumerServiceClient;

  public void createConsumerForSignup(ConsumerInfoForCreateRequestDto createRequestDto) {
    consumerServiceClient.createConsumerForSignup(createRequestDto);
  }
}
