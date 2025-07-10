package com.example.demo.config;

import com.example.demo.dto.tokenDto.JwtToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    //key 값 저장
    public JwtTokenProvider(@Value("${jwt.secret}")String secretKey){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    //새로운 Token 발급
    public JwtToken generateToken(Authentication authentication){
        
        //사용자의 권한 목록 String Type로 반환
        String authorities = authentication.getAuthorities()//Collection Return
                .stream().
                map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        
        //Access Token  생성
        Date accessTokenExpires = new Date(now + 30*60 *1000);
        String accessToken = Jwts.builder().setSubject(authentication.getName())
                .claim("auth",authorities)
                .setExpiration(accessTokenExpires)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder().setExpiration(new Date(now + 7*24*60*60*1000))
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication getAuthentication(String accessToken){
        //Jwt 토큰 복호화
        Claims claims = parseClaims(accessToken);
        if(claims.get("auth") == null){
            throw  new RuntimeException("권한 정보가 없는 토큰입니다.");
        }


        Collection<? extends  GrantedAuthority>authorities
                = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)//auth의 각 문자열을 꺼내 배열로 만들고 각 문자열을 SimpleGrantedAuthority 타입 객체로 변환
                .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(),"",authorities);
        return new UsernamePasswordAuthenticationToken(principal,"",authorities);

    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (SecurityException | MalformedJwtException e){
            log.info("Invalid JWT Token",e);
        }catch (ExpiredJwtException e){
            log.info("Expired JWT Token");
        }catch(UnsupportedJwtException e){
            log.info("Unsupported JWT Token",e);
        }catch (IllegalArgumentException e){
            log.info("JWT Claims String is Empty",e);
        }
        return false;
    }



    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()//ParseBuilder 생성
                    .setSigningKey(key)//서명 검즘을 위한 비밀 키 등록
                    .build()//Parser 생성
                    .parseClaimsJws(accessToken) //입력 받은 토큰 검사
                    .getBody(); //Claims 추출

        } catch (ExpiredJwtException e) {//토큰이 만료
            return e.getClaims(); // 만료돼도 Claims는 Return
        }

    }




}
