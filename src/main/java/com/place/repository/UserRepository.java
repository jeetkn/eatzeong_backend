package com.place.repository;

import com.place.dto.UserDto;
import com.place.service.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

    @Resource(name="com.place.service.mapper.UserMapper")
    UserMapper mapper;

    public int selectEmailCount(UserDto userDto) throws Exception {
        return mapper.selectEmailCount(userDto);
    }

    public void insertGeneralUser(UserDto userDto) throws Exception {
        mapper.insertGeneralUser(userDto);
    }

    public String selectPassword(UserDto userDto) throws Exception {
        return mapper.selectPassword(userDto);
    }

    public int selectPasswordResetCnt(UserDto userDto) throws Exception{
        return mapper.selectPasswordResetCnt(userDto);
    }

    public void updateLoginSuccess(UserDto userDto) throws Exception{
        mapper.updateLoginSuccess(userDto);
    }

    public void updateLoginFailed(UserDto userDto) throws Exception{
        mapper.updateLoginFailed(userDto);
    }

    public void updatePassword(UserDto userDto) throws Exception{
        mapper.updatePassword(userDto);
    }

    public List<Map<String, Object>> selectNotice() throws Exception {
        return mapper.selectNotice();
    }

    public List<Map<String, Object>> selectTerms()  throws Exception{
        return mapper.selectTerms();
    }

    public List<Map<String, Object>> selectFaqs() throws Exception{
        return mapper.selectFaqs();
    }

    public int selectGeneralUser(UserDto userDto) throws Exception{
        return mapper.selectGeneralUser(userDto);
    }

    public List<String> findId(UserDto userDto) throws Exception{
        return mapper.findId(userDto);
    }

    public int findPassword(UserDto userDto) throws Exception{
        return mapper.findPassword(userDto);
    }

    public void accountClose(UserDto userDto) throws Exception{
        mapper.accountClose(userDto);
    }

    public void updateProfileImage(Map<String, String> attachment_map) throws Exception{
        mapper.updateProfileImage(attachment_map);
    }

    public UserDto selectUser(UserDto userDto) throws Exception{
        return mapper.selectUser(userDto);
    }

    public UserDto selectUserInfo(UserDto userDto) throws Exception{
        return mapper.selectUserInfo(userDto);
    }
}
