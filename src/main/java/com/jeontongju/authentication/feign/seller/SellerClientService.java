package com.jeontongju.authentication.feign.seller;

import com.jeontongju.authentication.dto.temp.SellerInfoForCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerClientService {

  private final SellerServiceClient sellerServiceClient;

  public void createSellerForSignup(SellerInfoForCreateRequestDto createRequestDto) {
    sellerServiceClient.createSellerForSignup(createRequestDto);
  }

  public Long getCountOfApprovalWaitingSeller() {
    return sellerServiceClient.getCountOfApprovalWaitingSeller().getData();
  }
}
