package com.jeontongju.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MemberInfoForAdminManagingResponseDto {

  private Integer teenage;
  private Integer twenty;
  private Integer thirty;
  private Integer fortyOver;
  private List<Long> consumers;
  private List<Long> sellers;
}
