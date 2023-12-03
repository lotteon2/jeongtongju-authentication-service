package com.jeontongju.authentication.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImpTokenResponse {
    private String access_token;
}
