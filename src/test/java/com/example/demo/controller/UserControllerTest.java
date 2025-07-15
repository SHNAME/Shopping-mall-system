package com.example.demo.controller;

import com.example.demo.config.RedisTestConfig;
import com.example.demo.domain.Address;
import com.example.demo.domain.UserData;
import com.example.demo.dto.signDto.SendCodePasswordRequest;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(RedisTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDataRepository userDataRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    private static final  String EMAIL = "test@naver.com";
    private static final String PHONE_NUMBER = "test@naver.com";
    //초기 세팅
    @BeforeEach
    void setUp(){
        UserData user = UserData.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode("test1234"))
                .nickname("테스트유저")
                .roles(List.of(Role.ROLE_USER))
                .build();
        Address address = new Address("testName", PHONE_NUMBER, "test_address");
        user.setAddress(address);
        userDataRepository.save(user);
    }


    @Test
    void LoginSuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", EMAIL);
        request.put("password", "test1234");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

    }

    //존재하지 않는 이메일로 로그인 시도
    @Test
    void LoginEmailDuplication() throws Exception{
        Map<String, String> request = new HashMap<>();
        request.put("email", "asdasd@naver.com");
        request.put("password", "test1234");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    //비밀번호 불일치
    @Test
    void LoginPassWordMismatch() throws Exception{
        Map<String, String> request = new HashMap<>();
        request.put("email", EMAIL);
        request.put("password", "test12121234");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signUpSuccess() throws Exception {
        Map<String,String> signUpRequest = new HashMap<>();
        signUpRequest.put("email","newUser@naver.com");
        signUpRequest.put("password","new1234");
        signUpRequest.put("nickName","test");
        signUpRequest.put("name","testName");
        signUpRequest.put("phoneNumber","01012345678");
        signUpRequest.put("address_line","address_line");

        mockMvc.perform(post("/auth/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        Optional<UserData> savedUser = userDataRepository.findByEmail(signUpRequest.get("email"));
        Assertions.assertThat(savedUser.isPresent());
        Assertions.assertThat(savedUser.get().getEmail()).isEqualTo("newUser@naver.com");
        Assertions.assertThat(savedUser.get().getNickname()).isEqualTo("test");

    }
    //이메일 중복 발생
    @Test
    void signUpFail() throws Exception {
        Map<String,String> signUpRequest = new HashMap<>();
        signUpRequest.put("email",EMAIL);
        signUpRequest.put("password","new1234");
        signUpRequest.put("nickName","test");
        signUpRequest.put("name","testName");
        signUpRequest.put("phoneNumber","01012345678");
        signUpRequest.put("address_line","address_line");

        mockMvc.perform(post("/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void emailCheckSuccess() throws  Exception{
        UserData user = userDataRepository.findByEmail(EMAIL).orElseThrow();
        String userPhoneNumber = user.getAddress().getPhoneNumber();

        Map<String,String> request = new HashMap<>();
        request.put("email",user.getEmail());
        request.put("phoneNumber",userPhoneNumber);

        mockMvc.perform(post("/auth/sms/sendCode/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isString());



    }

    //요청한 사용자가 없는 경우
    @Test
    void emailCheckFail() throws  Exception{
        Map<String,String> request = new HashMap<>();
        request.put("email","fail@naver.com");
        request.put("phoneNumber","111111111");
        mockMvc.perform(post("/auth/sms/sendCode/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emailProveSuccess() throws Exception {
        UserData user = userDataRepository.findByEmail(EMAIL).orElseThrow();
        String userPhoneNumber = user.getAddress().getPhoneNumber();
        String code = "AABBCC1234";
        redisTemplate.opsForValue().set("emailAuth:Phone" +userPhoneNumber,code, Duration.ofMinutes(3));
        when(redisTemplate.opsForValue().get("emailAuth:Phone" +userPhoneNumber)).thenReturn(code);
        Map<String,String> request = new HashMap<>();
        request.put("phoneNumber",userPhoneNumber);
        request.put("code",code);

        mockMvc.perform(post("/auth/sms/proofCode/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                 .andExpect(status().isOk());
    }

    //인증번호 실패
    @Test
    void emailProveFail() throws  Exception{
        UserData user = userDataRepository.findByEmail(EMAIL).orElseThrow();
        String userPhoneNumber = user.getAddress().getPhoneNumber();
        String code = "AABBCC1234";
        redisTemplate.opsForValue().set("emailAuth:Phone" +userPhoneNumber,code, Duration.ofMinutes(3));
        when(redisTemplate.opsForValue().get("emailAuth:Phone" +userPhoneNumber)).thenReturn(code);
        Map<String,String> request = new HashMap<>();
        request.put("phoneNumber",userPhoneNumber);
        request.put("code","TEST1234");

        mockMvc.perform(post("/auth/sms/proofCode/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

    }


    @Test
    void passwordCheckSuccess() throws Exception {
        UserData user = userDataRepository.findByEmail(EMAIL).orElseThrow();
        String userPhoneNumber = user.getAddress().getPhoneNumber();
        Map<String,String> request = new HashMap<>();
        request.put("email",user.getEmail());
        request.put("phoneNumber",userPhoneNumber);

        mockMvc.perform(post("/auth/sms/sendCode/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isString());
    }

    //해당 이메일 존재 하지 않는 경우
    @Test
    void passwordCheckFail() throws Exception {
        Map<String,String> request = new HashMap<>();
        request.put("email","Fail@gmail.com");
        request.put("phoneNumber",PHONE_NUMBER);

        mockMvc.perform(post("/auth/sms/sendCode/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void passwordProveSuccess() throws Exception{
        //given
        UserData user = userDataRepository.findByEmail(EMAIL).orElseThrow();
        String userPhoneNumber = user.getAddress().getPhoneNumber();
        String code = "AABBCC1234";
        String key = "passwordAuth:Phone" +userPhoneNumber;
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(3));
        when(redisTemplate.opsForValue().get(key)).thenReturn(code);
        Map<String,String> request = new HashMap<>();
        request.put("phoneNumber",userPhoneNumber);
        request.put("code",code);

        //when
        //then
        mockMvc.perform(post("/auth/sms/proofCode/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    //인증번호 실패
    @Test
    void passwordProveFail() throws Exception{
        //given
        UserData user = userDataRepository.findByEmail(EMAIL).orElseThrow();
        String userPhoneNumber = user.getAddress().getPhoneNumber();
        String code = "AABBCC1234";
        String key = "passwordAuth:Phone" +userPhoneNumber;
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(3));
        when(redisTemplate.opsForValue().get(key)).thenReturn(code);
        Map<String,String> request = new HashMap<>();
        request.put("phoneNumber",userPhoneNumber);
        request.put("code","AABBCC12");

        //when
        //then
        mockMvc.perform(post("/auth/sms/proofCode/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }




}