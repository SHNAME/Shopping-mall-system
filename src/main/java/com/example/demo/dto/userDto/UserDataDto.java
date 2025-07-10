package com.example.demo.dto.userDto;


import com.example.demo.domain.Address;
import com.example.demo.domain.UserData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class UserDataDto {

    private Long id;
    private String email;
    private String nickname;
    private String name;
    private String address_line;
    private String phoneNumber;

    public static UserDataDto toDto(UserData user){
        Address userAddress = user.getAddress();
        return UserDataDto.builder()
                .id(user.getId())
                .name(userAddress.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .address_line(userAddress.getAddress_line())
                .phoneNumber(userAddress.getPhoneNumber())
                .build();
    }





}
