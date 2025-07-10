package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.domain.UserData;
import com.example.demo.dto.signDto.SignUpDto;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//Login 및 SingUp Unit Test
@ExtendWith(MockitoExtension.class)
class UserCertificationServiceTest {

    @InjectMocks
    private UserCertificationService userCertificationService;
    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManagerBuilder managerBuilder;


    @Test
    void signIn() {
    }

    @Test
    void signUpSuccess() {
        //given
        SignUpDto dto = SignUpDto.builder()
                .email("test@naver.com")
                .password("1234")
                .nickName("testNickName")
                .name("testName")
                .phoneNumber("12345678")
                .address_line("test_address")
                .build();



        when(userDataRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword()))
                .thenReturn("1234");

        UserData savedUser = dto.toEntity("encodedPassword", List.of(Role.ROLE_USER));
        when(userDataRepository.save(any(UserData.class)))
                .thenReturn(savedUser);

        //when

        UserDataDto result = userCertificationService.signUp(dto);

        //then
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getName()).isEqualTo(dto.getName());

    }

    @Test
    //이메일 중복으로 인한 테스트 실패
    void signUpFail(){
        SignUpDto dto = SignUpDto.builder()
                .email("test@naver.com")
                .password("1234")
                .nickName("testNickName")
                .name("testName")
                .phoneNumber("12345678")
                .address_line("test_address")
                .build();

        when(userDataRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(mock(UserData.class)));
        assertThrows(IllegalArgumentException.class,() ->{
            userCertificationService.signUp(dto);
        });

        verify(userDataRepository,never()).save(any());
    }
}