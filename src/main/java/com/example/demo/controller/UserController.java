package com.example.demo.controller;

import com.example.demo.dto.signDto.SignUpDto;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.signDto.SignInDto;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.service.UserCertificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class UserController {
    private final UserCertificationService userCertificationService;


    @PostMapping("/sign-in")
    public JwtToken signIn(@RequestBody SignInDto signInDto){
        String email = signInDto.getEmail();
        String password = signInDto.getPassword();
        JwtToken jwtToken = userCertificationService.signIn(email,password);
        log.info("request email = {}, password = {}",email,password);
        log.info("jwtToken accessToken = {}, refreshToken = {}",jwtToken.getAccessToken(),jwtToken.getRefreshToken());
        return  jwtToken;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDataDto>signUp(@RequestBody SignUpDto signUpDto){
        UserDataDto savedUserDataDto = userCertificationService.signUp(signUpDto);
        return ResponseEntity.ok(savedUserDataDto);
    }



    @PostMapping("/test")
    public String test(){
        return "success";
    }


}
