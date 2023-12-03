package com.jeontongju.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class SuccessFormat<T> {

  private final Integer code;
  private final String message;
  private final String detail;

  @JsonInclude(Include.NON_NULL)
  private final T data;
}
