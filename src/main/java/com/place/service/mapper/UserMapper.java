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

    int selectGeneralUser(UserDto userDto) throws Exception;

    List<String> findId(UserDto userDto) throws Exception;

    int findPassword(UserDto userDto) throws Exception;

    void accountClose(UserDto userDto) throws Exception;

    void updateProfileImage(Map<String, String> attachment_map) throws Exception;

    UserDto selectUser(UserDto userDto) throws Exception;

    UserDto selectUserInfo(UserDto userDto) throws Exception;

    void updateBookmarkDelFlag(UserDto userDto) throws Exception;

    void updateAppReviewDelFlag(UserDto userDto) throws Exception;

    void deleteLike(UserDto userDto) throws Exception;

    void deleteBlackList(UserDto userDto) throws Exception;

    void updateNickname(UserDto userDto) throws Exception;

    void updatePhone(UserDto userDto) throws Exception;

    List<Map<String, Object>> selectTermsDetailExecuteDate(String terms_code) throws Exception;

    List<Map<String, Object>> selectTermsDetail(String terms_code) throws Exception;

    String selectLoginDate(UserDto userDto) throws Exception;
}
