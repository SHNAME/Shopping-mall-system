package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor
@Data
@Builder
@NonNull
public class JwtToken {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}
