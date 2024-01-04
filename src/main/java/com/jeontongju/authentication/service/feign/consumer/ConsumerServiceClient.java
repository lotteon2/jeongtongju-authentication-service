package com.jeontongju.authentication.service.feign.consumer;

import com.jeontongju.authentication.dto.temp.*;
import io.github.bitbox.bitbox.dto.ImpAuthInfoForUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "consumer-service")
public interface ConsumerServiceClient {

  @PostMapping("/consumers")
  FeignFormat<Void> createConsumerForSignup(ConsumerInfoForCreateRequestDto createRequestDto);

  @PostMapping("/consumers/oauth")
  FeignFormat<Void> createConsumerForSignupBySns(
      ConsumerInfoForCreateBySnsRequestDto createBySnsRequestDto);

  @PutMapping("/consumers/account-consolidation")
  FeignFormat<Void> updateConsumerForAccountConsolidation(
      ConsumerInfoForAccountConsolidationDto accountConsolidationDto);

  @PutMapping("/consumers/adult-certification")
  FeignFormat<Void> updateConsumerByAuth19(ImpAuthInfoForUpdateDto impAuthInfoDto);
}
