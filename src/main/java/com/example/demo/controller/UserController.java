package com.example.demo.controller;

import com.example.demo.dto.signDto.SignUpDto;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.signDto.SignInDto;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.service.UserCertificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {
    private final UserCertificationService userCertificationService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody SignInDto signInDto){
        String email = signInDto.getEmail();
        String password = signInDto.getPassword();
        try{
            JwtToken jwtToken = userCertificationService.login(email,password);
            return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
        }catch (AuthenticationException ex){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<?>signUp(@RequestBody SignUpDto signUpDto){
        try{
            UserDataDto savedUserDataDto = userCertificationService.signUp(signUpDto);
            return ResponseEntity.ok(savedUserDataDto);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일 중복이 발생했습니다");
        }

    }


}
