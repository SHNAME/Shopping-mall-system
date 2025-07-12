package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.signDto.SignUpDto;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCertificationService {
    private final UserDataRepository userDataRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public JwtToken login(String email, String passWord){
        //Spring Security에서 사용하는 인증용 토큰 객체 생성 -> 아직 인증된 상태 x
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,passWord);
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(
                    authenticationToken);
            return jwtTokenProvider.generateToken(authentication);
    }

    //회원가입 요청을 받아 DB에 저장하고 UserDataDto 반환
    @Transactional
    public UserDataDto signUp(SignUpDto signUpDto){
        if(userDataRepository.findByEmail(signUpDto.getEmail()).isPresent()){
            throw new IllegalArgumentException();
        }
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ROLE_USER);
        return UserDataDto.toDto(userDataRepository.save(signUpDto.toEntity(encodedPassword,roles)));
    }



}
