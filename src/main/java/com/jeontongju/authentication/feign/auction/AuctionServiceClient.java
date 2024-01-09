package com.jeontongju.authentication.feign.auction;

import io.github.bitbox.bitbox.dto.FeignFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auction-service")
public interface AuctionServiceClient {

  @GetMapping("/auction-products/approval-wait")
  FeignFormat<Long> getCountOfApprovalWaitingProduct();
}
