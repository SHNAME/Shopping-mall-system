package com.example.demo.config;

import com.example.demo.domain.Address;
import com.example.demo.domain.UserData;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class InitUserData {

    private final PasswordEncoder passwordEncoder;

    //로그인 Test를 위한 초기 데이터 삽입
    @Bean
    public CommandLineRunner initTestUser(UserDataRepository userDataRepository) {
        return args -> {
            if (userDataRepository.findByEmail("test1234@gamil.com").isEmpty()) {
                UserData user = UserData.builder()
                        .email("test1234@gamil.com")
                        .password(passwordEncoder.encode("1234"))  // 인코딩된 비밀번호
                        .nickname("테스트유저")
                        .roles(Collections.singletonList(Role.ROLE_USER))
                        .build();

                Address address = new Address("name","01011112222","testAddress");
                user.setAddress(address);
                userDataRepository.save(user);

            }
        };
    }

}
