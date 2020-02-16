package com.place.service.interfaces;

import com.place.dto.UserDto;
import org.springframework.http.codec.multipart.FilePart;

import java.util.List;
import java.util.Map;

public interface UserServiceInterface{

    Map<String, Object> insertGeneralUser(UserDto request_param) throws Exception;

    boolean getGeneralEmailCheck(UserDto userDto) throws Exception;

    Map<String, Object> generalSignIn(UserDto userDto) throws Exception;

    void updatePassword(UserDto userDto) throws Exception;

    List<Object> selectNotice() throws Exception;

    List<Object> selectTerms() throws Exception;

    List<Object> selectFaqs() throws Exception;

    List<String> findId(UserDto userDto) throws Exception;

    Map<String, Object> finePassword(UserDto userDto) throws Exception;

    Map<String, Object> accountClose(UserDto userDto) throws Exception;

    Map<String, Object> updateProfile(UserDto userDto, FilePart file) throws Exception;

    Map<String, Object> selectUserInfo(UserDto userDto) throws Exception;
}
