package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final  JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //Header에서 Token 추출
        String token =  resolveToken((HttpServletRequest)servletRequest);
        
        //2.토큰 유효성 검사
        if(token !=null && jwtTokenProvider.validateToken(token)){
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }


    private String resolveToken(HttpServletRequest request){

        //Header에서 Token 추출 Barer ~~
        String barerToken = request.getHeader("Authorization");
        //null, 빈 문자열, 공백만 있는 문자열이 아닌지 체크, Bearer로 시작하는지 확인
        if(StringUtils.hasText(barerToken) && barerToken.startsWith("Bearer")){
            return barerToken.substring(7);
        }
        //잘못된 형식인 경우
        return null;
    }

}
