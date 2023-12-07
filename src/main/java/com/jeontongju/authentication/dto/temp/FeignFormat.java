package com.jeontongju.authentication.dto.temp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeignFormat<T> {

    private final Integer code;
    private final String message;
    private final String detail;
    private final String failure;
    private final T data;
}
