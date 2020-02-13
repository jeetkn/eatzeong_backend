package com.place.dto;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String nickname;
    private String birthday;
    private String password;
    private String gender;
    private String phone_number;
    private String sns_division;
}
