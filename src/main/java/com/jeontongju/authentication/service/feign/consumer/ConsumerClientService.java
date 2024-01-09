package com.jeontongju.authentication.service.feign.consumer;

import com.jeontongju.authentication.dto.temp.ConsumerInfoForAccountConsolidationDto;
import com.jeontongju.authentication.dto.temp.ConsumerInfoForCreateBySnsRequestDto;
import io.github.bitbox.bitbox.dto.ConsumerInfoForCreateRequestDto;
import io.github.bitbox.bitbox.dto.ImpAuthInfoForUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumerClientService {

  private final ConsumerServiceClient consumerServiceClient;

  @Transactional
  public void createConsumerForSignup(ConsumerInfoForCreateRequestDto createRequestDto) {
    consumerServiceClient.createConsumerForSignup(createRequestDto);
  }

  @Transactional
  public void createConsumerForSignupBySns(
      ConsumerInfoForCreateBySnsRequestDto createBySnsRequestDto) {
    consumerServiceClient.createConsumerForSignupBySns(createBySnsRequestDto);
  }

  @Transactional
  public void updateConsumerForAccountConsolidation(
      ConsumerInfoForAccountConsolidationDto accountConsolidationDto) {

    consumerServiceClient.updateConsumerForAccountConsolidation(accountConsolidationDto);
  }

  @Transactional
  public void updateConsumerByAuth19(ImpAuthInfoForUpdateDto impAuthInfoDto) {

    consumerServiceClient.updateConsumerByAuth19(impAuthInfoDto);
  }
}
