package com.example.demo.dto.signDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class SendCodePasswordRequest {
    String email;
    String phoneNumber;
}
