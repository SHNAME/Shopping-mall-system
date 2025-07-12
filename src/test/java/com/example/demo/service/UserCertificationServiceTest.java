package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.domain.UserData;
import com.example.demo.dto.signDto.SignUpDto;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
    private Authentication authentication;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;


    @Test
    void LoginSuccess() {
        String email = "test@naver.com";
        String password = "test1234";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        JwtToken token = new JwtToken("Bearer","access-token","refresh-token");

        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(authToken)).willReturn(authentication);
        given(jwtTokenProvider.generateToken(authentication)).willReturn(token);

        JwtToken result = userCertificationService.login(email, password);



        verify(authenticationManager).authenticate(authToken);
        verify(jwtTokenProvider).generateToken(authentication);
        assertThat(result.getGrantType()).isEqualTo(token.getGrantType());
        assertThat(result.getRefreshToken()).isEqualTo(token.getRefreshToken());
        assertThat(result.getAccessToken()).isEqualTo(token.getAccessToken());

    }
    @Test
    void LoginFail(){
        String email = "test@naver.com";
        String password = "test1234";
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(authToken)).willThrow(new BadCredentialsException("login Fail"));
        assertThrows(BadCredentialsException.class,()->{
            userCertificationService.login(email,password);
        });



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