package com.jeontongju.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ErrorFormat {

    private final Integer code;
    private final String message;
    private final String detail;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String failure;
}
