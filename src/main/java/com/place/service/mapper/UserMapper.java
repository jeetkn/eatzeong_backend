package com.place.service.mapper;

import com.place.dto.UserDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("com.place.service.mapper.UserMapper")
public interface UserMapper {

    int selectEmailCount(UserDto userDto) throws Exception;

    void insertGeneralUser(UserDto userDto) throws Exception;

    String selectPassword(UserDto userDto) throws Exception;

    int selectPasswordResetCnt(UserDto userDto) throws Exception;

    void updateLoginSuccess(UserDto userDto) throws Exception;

    void updateLoginFailed(UserDto userDto) throws Exception;

    void updatePassword(UserDto userDto);

    List<Map<String, Object>> selectNotice() throws Exception;

    List<Map<String, Object>> selectTerms() throws Exception;

    List<Map<String, Object>> selectFaqs() throws Exception;
}
