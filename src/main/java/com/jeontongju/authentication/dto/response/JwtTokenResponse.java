package com.jeontongju.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class JwtTokenResponse {

    private String accessToken;
    private String refreshToken;
}
