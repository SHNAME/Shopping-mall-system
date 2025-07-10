package com.example.demo.dto.signDto;

import com.example.demo.domain.Address;
import com.example.demo.domain.UserData;
import com.example.demo.en.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SignUpDto {
    String email;
    String password;
    String nickName;
    String name;
    String phoneNumber;
    String address_line;

    public UserData toEntity(String encodedPassword, List<Role> roles){
       UserData newUser = UserData.builder().email(email)
               .password(encodedPassword).nickname(nickName)
               .roles(roles)
               .build();
        Address userAddress = new Address(name,phoneNumber,address_line);
        newUser.setAddress(userAddress);
        return newUser;
    }



}
