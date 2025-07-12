package com.example.demo.controller;

import com.example.demo.domain.UserData;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserDataRepository userDataRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    //초기 세팅
    @BeforeEach
    void setUp(){
        UserData user = UserData.builder()
                .email("test@naver.com")
                .password(passwordEncoder.encode("test1234"))
                .nickname("테스트유저")
                .roles(List.of(Role.ROLE_USER))
                .build();
        userDataRepository.save(user);
    }


    @Test
    void LoginSuccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@naver.com");
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
        request.put("email", "test@naver.com");
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
        signUpRequest.put("email","test@naver.com");
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
}