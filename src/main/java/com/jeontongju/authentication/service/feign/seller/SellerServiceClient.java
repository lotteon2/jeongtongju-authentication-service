package com.jeontongju.authentication.service.feign.seller;

import com.jeontongju.authentication.dto.request.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.temp.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "seller-service")
public interface SellerServiceClient {

  @PostMapping("/sellers")
  FeignFormat<Void> createSellerForSignup(SellerInfoForCreateRequestDto createRequestDto);
}
