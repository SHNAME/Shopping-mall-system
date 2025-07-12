package com.example.demo.dto.signDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CodeProofRequest {
    String phoneNumber;
    String code;
}
