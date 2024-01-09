package com.jeontongju.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SiteSituationForAdminManagingResponseDto {

  private Long waitingApprovalSellerCnts;
  private int newSellerCnts;
  private int newConsumerCnts;
  private int deletedMemberCnts;
  private Long waitingApprovalAuctionCnts;
}
