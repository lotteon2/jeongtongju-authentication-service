package com.jeontongju.authentication.service.feign.seller;

import com.jeontongju.authentication.dto.request.SellerInfoForCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerClientService {

  private final SellerServiceClient sellerServiceClient;

  public void createSellerForSignup(SellerInfoForCreateRequestDto createRequestDto) {
    sellerServiceClient.createSellerForSignup(createRequestDto);
  }
}
