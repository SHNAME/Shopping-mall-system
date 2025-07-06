package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.JwtToken;
import com.example.demo.repository.UserDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCertificationService {
    private final UserDataRepository userDataRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public JwtToken signIn(String email,String passWord){

        //Spring Security에서 사용하는 인증용 토큰 객체 생성 -> 아직 인증된 상태 x
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(email,passWord);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(
                authenticationToken);

        return jwtTokenProvider.generateToken(authentication);

    }



}
