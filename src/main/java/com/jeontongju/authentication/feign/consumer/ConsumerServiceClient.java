package com.jeontongju.authentication.feign.consumer;

import com.jeontongju.authentication.dto.temp.*;
import io.github.bitbox.bitbox.dto.AgeDistributionForShowResponseDto;
import io.github.bitbox.bitbox.dto.ConsumerInfoForCreateRequestDto;
import io.github.bitbox.bitbox.dto.ImpAuthInfoForUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @GetMapping("/consumers/age-distribution")
  FeignFormat<AgeDistributionForShowResponseDto> getAgeDistributionForAllMembers();

  @PutMapping("/consumers/{consumerId}/withdrawal")
  FeignFormat<Void> delete(@PathVariable Long consumerId);
}
