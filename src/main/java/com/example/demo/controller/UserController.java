package com.example.demo.controller;

import com.example.demo.dto.signDto.*;
import com.example.demo.dto.tokenDto.JwtToken;
import com.example.demo.dto.userDto.UserDataDto;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody SignInDto signInDto){
        String email = signInDto.getEmail();
        String password = signInDto.getPassword();
        try{
            JwtToken jwtToken = userService.login(email,password);
            return ResponseEntity.status(HttpStatus.OK).body(jwtToken);
        }catch (AuthenticationException ex){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<?>signUp(@RequestBody SignUpDto signUpDto){
        try{
            UserDataDto savedUserDataDto = userService.signUp(signUpDto);
            return ResponseEntity.ok(savedUserDataDto);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이메일 중복이 발생했습니다");
        }

    }

    @PostMapping("/sms/sendCode/email")
    public ResponseEntity<?> emailCheck(@RequestBody SendCodeEmailRequest request)
    {
        try{
            SendCodeResponse result = userService.emailCheck(request);
            return ResponseEntity.ok(result);
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/sms/proofCode/email")
    public ResponseEntity<?>emailProve(@RequestBody CodeProofRequest request){
        try{
            CodeProofResponseEmail result = userService.emailProve(request);
            if(result.isSuccess())
            {
                return ResponseEntity.ok(result);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
        }catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }

    }

    @PostMapping("/sms/sendCode/password")
    public ResponseEntity<?>passwordCheck(@RequestBody SendCodePasswordRequest request)
    {
        try{
            SendCodeResponse result = userService.passwordCheck(request);
            return ResponseEntity.ok(result);
        }catch (NullPointerException | IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

    }

    @PostMapping("/sms/proofCode/password")
    public ResponseEntity<?>passwordProve(@RequestBody CodeProofRequest request){
        try{
            CodeProofResponsePassword result = userService.passwordProve(request);
            if(result.isVerified())  return ResponseEntity.ok(result);
            else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @PatchMapping("/resetPassword")
    public ResponseEntity<?>resetPassword(@RequestBody ResetPasswordRequest request){
        try{
            ResetPasswordResponse result = userService.resetPassword(request);
            log.info(result.toString());
            return ResponseEntity.ok().body(result);
        } catch(IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (RedisConnectionFailureException e){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }



}
