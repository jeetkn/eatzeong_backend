package com.place.service.interfaces;

import com.place.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserServiceInterface{

    void insertGeneralUser(UserDto request_param) throws Exception;

    boolean getGeneralEmailCheck(UserDto userDto) throws Exception;

    Map<String, Object> generalSignIn(UserDto userDto) throws Exception;

    void updatePassword(UserDto userDto) throws Exception;

    List<Object> selectNotice() throws Exception;

    List<Object> selectTerms() throws Exception;

    List<Object> selectFaqs() throws Exception;
}
