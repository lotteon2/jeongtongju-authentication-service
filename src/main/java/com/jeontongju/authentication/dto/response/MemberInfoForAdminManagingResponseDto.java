package com.jeontongju.authentication.dto.response;

import java.sql.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MemberInfoForAdminManagingResponseDto {

  private Double teenage;
  private Double twenty;
  private Double thirty;
  private Double fortyOver;
  private Map<Date, Long> consumers;
  private Map<Date, Long> sellers;
}
