package com.jeontongju.authentication.dto.temp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ResponseFormat<T> {

  private final Integer code;
  private final String message;
  private final String detail;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String failure;

  @JsonInclude(Include.NON_NULL)
  private final T data;
}
