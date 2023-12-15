package com.jeontongju.authentication.dto.temp;

import com.jeontongju.authentication.dto.request.SellerInfoForSignUpRequestDto;
import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SellerInfoForCreateRequestDto {

  private Long memberId;
  private String email;
  private String storeName;
  private String storeDescription;
  private String storeImageUrl;
  private String storePhoneNumber;
  private String businessmanName;
  private String businessmanPhoneNumber;
}
