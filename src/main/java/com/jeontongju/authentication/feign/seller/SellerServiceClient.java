package com.jeontongju.authentication.feign.seller;

import com.jeontongju.authentication.dto.temp.SellerInfoForCreateRequestDto;
import com.jeontongju.authentication.dto.temp.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "seller-service")
public interface SellerServiceClient {

  @PostMapping("/sellers")
  FeignFormat<Void> createSellerForSignup(SellerInfoForCreateRequestDto createRequestDto);

  @GetMapping("/sellers/approval-wait")
  FeignFormat<Long> getCountOfApprovalWaitingSeller();
}
