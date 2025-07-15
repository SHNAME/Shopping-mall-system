package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.domain.Address;
import com.example.demo.domain.UserData;
import com.example.demo.dto.signDto.*;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

//Login 및 SingUp Unit Test
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;
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

    @Mock
    private RedisTemplate<String,String> redisTemplate;

    @Mock
    private ValueOperations<String,String> valueOperations;

    private static  final String PREFIX="emailAuth:Phone";

    @Test
    void LoginSuccess() {
        String email = "test@naver.com";
        String password = "test1234";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        JwtToken token = new JwtToken("Bearer","access-token","refresh-token");

        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(authToken)).willReturn(authentication);
        given(jwtTokenProvider.generateToken(authentication)).willReturn(token);

        JwtToken result = userService.login(email, password);



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
            userService.login(email,password);
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

        UserDataDto result = userService.signUp(dto);

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
            userService.signUp(dto);
        });

        verify(userDataRepository,never()).save(any());
    }

    @Test
    void emailCheckSuccess(){
        //given
        String phoneNumber = "01012345678";
        String email = "test@example.com";
        SendCodeEmailRequest request = new SendCodeEmailRequest(phoneNumber);
        Address address = new Address("test",phoneNumber,"test_line");
        UserData userData = UserData.builder().email(email).build();
        userData.setAddress(address);

        when(userDataRepository.findByAddress_phoneNumber(phoneNumber)).thenReturn(Optional.of(userData));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        SendCodeResponse sendCodeResponse = userService.emailCheck(request);

        Assertions.assertThat(sendCodeResponse.getMessage()).isEqualTo("인증번호 발급 성공");
        Assertions.assertThat(sendCodeResponse.getCode()).isNotNull();

    }
    //요청한 사용자가 없는 경우
    @Test
    void emailCheckFail(){
        SendCodeEmailRequest request = new SendCodeEmailRequest( "010101010");
        when(userDataRepository.findByAddress_phoneNumber(request.getPhoneNumber())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.emailCheck(request));
    }

    @Test
    void emailProveSuccess(){
        String phone = "01012345678";
        String email = "test@naver.com";
        String code = "ABC12345";
        CodeProofRequest request = new CodeProofRequest(phone, code);
        UserData user = UserData.builder().email(email).build();
        Address address = new Address("test",phone,"test_addressLine");
        user.setAddress(address);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PREFIX +phone)).thenReturn(code);
        when(userDataRepository.findByAddress_phoneNumber(phone)).thenReturn(Optional.of(user));

        //when
        CodeProofResponseEmail response = userService.emailProve(request);

        assertTrue(response.isSuccess());
        assertTrue(response.getEmail().contains("*"));

    }

    //인증번호 불일치
    @Test
    void emailProveFail(){
        String phone = "01012345678";
        String email = "test@naver.com";
        String myCode = "ABC12345";
        String redisCode = "ABC11111";
        CodeProofRequest request = new CodeProofRequest(phone, myCode);
        UserData user = UserData.builder().email(email).build();
        Address address = new Address("test",phone,"test_addressLine");
        user.setAddress(address);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PREFIX +phone)).thenReturn(redisCode);

        CodeProofResponseEmail response = userService.emailProve(request);
        assertFalse(response.isSuccess());
        assertFalse(response.getEmail().contains("*"));

    }

    @Test
    void passwordCheckSuccess(){
        //given
        String phoneNumber = "01012345678";
        String email = "test@example.com";

        SendCodePasswordRequest request = new SendCodePasswordRequest(email,phoneNumber);
        UserData userData = UserData.builder().email(email).build();
        Address address = new Address("test",phoneNumber,"test_line");
        userData.setAddress(address);

        when(userDataRepository.findByEmail(email)).thenReturn(Optional.of(userData));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        //when
        SendCodeResponse result = userService.passwordCheck(request);

        //then
        Assertions.assertThat(result.getCode()).isNotNull();
        Assertions.assertThat(result.getMessage()).isEqualTo("인증번호 발송 성공");

    }

    //이메일이 DB에 존재하지 않는 경우
    @Test
    void passwordCheckFail(){
        //given
        String phoneNumber = "01012345678";
        String email = "test@example.com";

        SendCodePasswordRequest request = new SendCodePasswordRequest(email,phoneNumber);
        when(userDataRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        //then
        assertThrows(IllegalArgumentException.class, () ->userService.passwordCheck(request));

    }

    @Test
    void passwordProveSuccess(){
        //given
        String phoneNumber = "01012345678";
        String code = "AABBCC";
        CodeProofRequest request = new CodeProofRequest(phoneNumber, code);
        UserData userData = UserData.builder().build();
        Address address = new Address("test",phoneNumber,"test_line");
        userData.setAddress(address);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(code);
        when(userDataRepository.findByAddress_phoneNumber(phoneNumber)).thenReturn(Optional.of(userData));

        //when
        CodeProofResponsePassword result = userService.passwordProve(request);
        //then
        Assertions.assertThat(result.isVerified()).isTrue();
    }

    //인증번호가 틀린 경우
    @Test
    void passwordProveFail(){
        //given
        String phoneNumber = "01012345678";
        String code = "AABBCC";
        CodeProofRequest request = new CodeProofRequest(phoneNumber, code);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("AABBC");
        //when
        CodeProofResponsePassword result = userService.passwordProve(request);
        //then
        Assertions.assertThat(result.isVerified()).isFalse();

    }

}