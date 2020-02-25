package com.place.dto;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String user_id;
    private String nickname;
    private String birthday;
    private String password;
    private String gender;
    private String phone_number;
    private String sns_division;
    private String profile_image;
    private String join_date;
    private String join_time;
}
