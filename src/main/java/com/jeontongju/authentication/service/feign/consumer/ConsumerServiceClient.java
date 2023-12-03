package com.jeontongju.authentication.service.feign.consumer;

import com.jeontongju.authentication.dto.SuccessFeignFormat;
import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "consumer-service", url = "${endpoint.consumer-service}")
public interface ConsumerServiceClient {

  @PostMapping("/consumers")
  SuccessFeignFormat<?> createConsumerForSignup(ConsumerInfoForCreateRequestDto createRequestDto);
}
