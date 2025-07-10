package com.example.demo.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    //Api를 요청한 사용자의 이름을 Log에 남기기 위한 함수
    public static String getCurrentUsername(){
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null||authentication.getName() == null){
            throw new RuntimeException("No authentication information");
        }
        return authentication.getName();
    }


}
