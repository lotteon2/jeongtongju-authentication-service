package com.jeontongju.authentication.feign.auction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionClientService {

  private final AuctionServiceClient auctionServiceClient;

  public Long getCountOfApprovalWaitingProduct() {
    return auctionServiceClient.getCountOfApprovalWaitingProduct().getData();
  }
}
