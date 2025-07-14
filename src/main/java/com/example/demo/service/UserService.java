package com.example.demo.service;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.domain.Address;
import com.example.demo.domain.UserData;
import com.example.demo.dto.signDto.*;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.en.Role;
import com.example.demo.repository.UserDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserDataRepository userDataRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String EMAIL_PREFIX = "emailAuth:Phone";
    private static final String PASSWORD_PREFIX = "passwordAuth:Phone";
    private static final String RESET_PASSWORD_PREFIX ="reset-auth:email";

    @Transactional(readOnly = true)
    public JwtToken login(String email, String passWord) {
        //Spring Security에서 사용하는 인증용 토큰 객체 생성 -> 아직 인증된 상태 x
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, passWord);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(
                authenticationToken);
        return jwtTokenProvider.generateToken(authentication);
    }

    //회원가입 요청을 받아 DB에 저장하고 UserDataDto 반환
    @Transactional
    public UserDataDto signUp(SignUpDto signUpDto) {
        if (userDataRepository.findByEmail(signUpDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException();
        }
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ROLE_USER);
        return UserDataDto.toDto(userDataRepository.save(signUpDto.toEntity(encodedPassword, roles)));
    }

    public SendCodeResponse emailCheck(SendCodeEmailRequest request) {
        if(request.getPhoneNumber() == null){
            throw  new NullPointerException("휴대전화 번호를 제대로 입력하지 않았습니다.");
        }
        Optional<UserData> findUser = userDataRepository.findByAddress_phoneNumber(request.getPhoneNumber());

        if (findUser.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다.");
        }
        UserData user = findUser.get();
        String authCode = RandomStringUtils.randomAlphanumeric(8);
        redisTemplate.opsForValue().set(EMAIL_PREFIX + request.getPhoneNumber(), authCode, Duration.ofMinutes(3));

        return new SendCodeResponse("인증번호 발급 성공", authCode);

    }

    public CodeProofResponseEmail emailProve(CodeProofRequest request){
        if(request.getCode() == null || request.getPhoneNumber() == null){
            throw new NullPointerException("전화번호 또는 인증코드가 없습니다.");
        }
        try{
            String redisCode = redisTemplate.opsForValue().get(EMAIL_PREFIX +request.getPhoneNumber());
            if( redisCode !=null && redisCode.equals(request.getCode()))
            {
                Optional<UserData> optionalUser = userDataRepository.findByAddress_phoneNumber(request.getPhoneNumber());
                if (optionalUser.isEmpty()) {
                    throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                }
                String email = optionalUser.get().getEmail();
                return new CodeProofResponseEmail(true,maskEmail(email));
            }
            else{
                return new CodeProofResponseEmail(false,"이메일 인증 실패");
            }
        }catch (RedisConnectionFailureException e){
            log.error("Redis Connection Error = {}",e.getMessage());
            throw new IllegalStateException("잠시 후 다시 시도해주세요");
        }

    }

    public SendCodeResponse passwordCheck(SendCodePasswordRequest request) {
        if(request.getEmail() == null || request.getPhoneNumber()==null){
            throw new NullPointerException("전화번호 또는 인증코드가 없습니다.");
        }
        Optional<UserData> findUser = userDataRepository.findByEmail(request.getEmail());
        if (findUser.isEmpty()) {
            throw new IllegalArgumentException("인증번호 발송 성공");
        }
            UserData user = findUser.get();
            Address address = user.getAddress();
            if (!address.getPhoneNumber().equals(request.getPhoneNumber())) {
                throw new IllegalArgumentException("인증번호 발송 성공");
            }
            String authCode = RandomStringUtils.randomAlphanumeric(8);
            redisTemplate.opsForValue().set(PASSWORD_PREFIX + request.getPhoneNumber(), authCode, Duration.ofMinutes(3));
            return new SendCodeResponse("인증번호 발송 성공", authCode);
    }

    public CodeProofResponsePassword passwordProve(CodeProofRequest request){
        if(request.getCode() == null || request.getPhoneNumber() == null){
            throw new IllegalArgumentException("전화번호 또는 인증코드가 제대로 전송되지 않았습니다.");
        }
        try{
            String redisCode = redisTemplate.opsForValue().get(PASSWORD_PREFIX +request.getPhoneNumber());
            if( redisCode !=null && redisCode.equals(request.getCode()))
            {
                Optional<UserData> findUser = userDataRepository.findByAddress_phoneNumber(request.getPhoneNumber());
                if (findUser.isEmpty()) {
                    return new CodeProofResponsePassword(false,"인증번호 인증 실패");
                }
                UserData user = findUser.get();
                Address address = user.getAddress();
                if(!address.getPhoneNumber().equals(request.getPhoneNumber())){
                    return new CodeProofResponsePassword(false,"인증번호 인증 실패");
                }
                //만약 휴대 전화번호와 인증코드가 모두 일치하면 Redis DB에 인증 확인 상태 임시 저장 -> 5분 안에 비밀번호 재설정
                redisTemplate.opsForValue().set(RESET_PASSWORD_PREFIX+user.getEmail(),"true",Duration.ofMinutes(5));
                return new CodeProofResponsePassword(true,"인증 성공 완료");
            }
            else{
                return new CodeProofResponsePassword(false,"인증번호 인증 실패.");
            }
        }catch (RedisConnectionFailureException e){
            log.error("Redis Connection Error = {}",e.getMessage());
            throw new IllegalStateException("잠시 후 다시 시도해주세요");
        }

    }

    private String maskEmail(String email){
        int at = email.indexOf('@');
        //앞에 글자가 1개 이하인 경우
        if (at <= 1) return "*@" + email.substring(at + 1);
        String front = email.substring(0, 2);
        String masked = "*".repeat(at - 2);
        return front + masked + email.substring(at);

    }







}
