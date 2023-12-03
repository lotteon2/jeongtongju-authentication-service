package com.jeontongju.authentication.service.feign.seller;

import com.jeontongju.authentication.dto.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.SuccessFeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "seller-service", url = "${endpoint.seller-service}")
public interface SellerServiceClient {

  @PostMapping("/sellers")
  SuccessFeignFormat<?> createSellerForSignup(SellerInfoForCreateRequestDto createRequestDto);
}
