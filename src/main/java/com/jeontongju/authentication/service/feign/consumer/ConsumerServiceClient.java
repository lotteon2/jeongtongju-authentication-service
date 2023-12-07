package com.jeontongju.authentication.service.feign.consumer;

import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateByKakaoRequestDto;
import com.jeontongju.authentication.dto.request.ConsumerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.temp.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "consumer-service")
public interface ConsumerServiceClient {

  @PostMapping("/consumers")
  FeignFormat<Void> createConsumerForSignup(ConsumerInfoForCreateRequestDto createRequestDto);

  @PostMapping("/consumers/oauth")
  FeignFormat<Void> createConsumerForSignupByKakao(
      ConsumerInfoForCreateByKakaoRequestDto createByKakaoRequestDto);
}
