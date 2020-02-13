package com.place.service;

import com.place.dto.UserDto;
import com.place.repository.UserRepository;
import com.place.service.interfaces.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service("com.place.service.UserService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
@Slf4j
public class UserService implements UserServiceInterface {

    @Inject
    UserRepository user;

    @Override
    public void insertGeneralUser(UserDto userDto) throws Exception{
        userDto.setSns_division("C");
        log.info(userDto.toString());

        user.insertGeneralUser(userDto);
    }

    @Override
    public boolean getGeneralEmailCheck(UserDto userDto) throws Exception {
        log.info("email : {}", userDto);
        int count = user.selectEmailCount(userDto);
        if(count > 0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public Map<String, Object> generalSignIn(UserDto userDto) throws Exception {
        Map<String, Object> return_map = new HashMap<>();
        log.info("userDto : {}", userDto.toString());
        if(user.selectEmailCount(userDto) > 0 ) {
            if (user.selectPasswordResetCnt(userDto) > 4) {
                return_map.put("result", false);
                return_map.put("result_message", "로그인 5회 연속 실패, 비밀번호 초기화 해주시기 바랍니다.");
            } else {
                String password = user.selectPassword(userDto);
                if (userDto.getPassword().equals(password)) {
                    user.updateLoginSuccess(userDto);
                    return_map.put("result", true);
                    return_map.put("result_message", "로그인 성공");
                } else {
                    user.updateLoginFailed(userDto);
                    return_map.put("result", false);
                    return_map.put("result_message", "로그인 실패");
                }
            }
        }else{
            return_map.put("result", false);
            return_map.put("result_message", "로그인 실패");
        }

        return return_map;
    }

    @Override
    public void updatePassword(UserDto userDto) throws Exception{
        user.updatePassword(userDto);
    }

    @Override
    public List<Object> selectNotice() throws Exception{
        List<Map<String, Object>> result_map = new ArrayList<>();
        List<Object> return_map = new ArrayList<>();

        result_map = user.selectNotice();
        result_map.stream()
//                .sorted(Comparator.comparing(c -> c.get("add_date").toString()))      // 입력 순 정렬
                .sorted((o1, o2) -> o2.get("add_date").toString().compareTo(o1.get("add_date").toString()))     // 최근 순 정렬
                .forEach(res -> {
                    log.info(res.toString());
                    Map<String, String> temp_map = new HashMap<>();
                    temp_map.put("notice_id", res.get("notice_id").toString());
                    temp_map.put("subject", res.get("notice_subject").toString());
                    temp_map.put("content", res.get("notice_contents").toString());
                    temp_map.put("add_date", res.get("add_date").toString());
                    temp_map.put("add_time", res.get("add_time").toString());
                    temp_map.put("user_id", res.get("user_id").toString());
                    return_map.add(temp_map);
                });

        return return_map;
    }

    @Override
    public List<Object> selectTerms() throws Exception{
        List<Map<String, Object>> result_map = new ArrayList<>();
        List<Object> return_map = new ArrayList<>();

        result_map = user.selectTerms();
        result_map.stream()
                .sorted(Comparator.comparing(c -> c.get("add_date").toString()))      // 입력 순 정렬
//                .sorted((o1, o2) -> o2.get("add_date").toString().compareTo(o1.get("add_date").toString()))     // 최근 순 정렬
                .forEach(res -> {
                    log.info(res.toString());
                    Map<String, String> temp_map = new HashMap<>();
                    temp_map.put("terms_id", res.get("terms_id").toString());
                    temp_map.put("terms_contents", res.get("terms_contents").toString());
                    temp_map.put("terms_division", res.get("terms_division").toString());
                    temp_map.put("add_date", res.get("add_date").toString());
                    temp_map.put("add_time", res.get("add_time").toString());
                    return_map.add(temp_map);
                });

        return return_map;
    }

    @Override
    public List<Object> selectFaqs() throws Exception{
        List<Map<String, Object>> result_map = new ArrayList<>();
        List<Object> return_map = new ArrayList<>();

        result_map = user.selectFaqs();
        result_map.stream()
                .sorted(Comparator.comparing(c -> c.get("add_date").toString()))      // 입력 순 정렬
//                .sorted((o1, o2) -> o2.get("add_date").toString().compareTo(o1.get("add_date").toString()))     // 최근 순 정렬
                .forEach(res -> {
                    log.info(res.toString());
                    Map<String, String> temp_map = new HashMap<>();
                    temp_map.put("faq_id", res.get("faq_id").toString());
                    temp_map.put("faq_subject", res.get("faq_subject").toString());
                    temp_map.put("faq_contents", res.get("faq_contents").toString());
                    temp_map.put("add_date", res.get("add_date").toString());
                    temp_map.put("add_time", res.get("add_time").toString());
                    return_map.add(temp_map);
                });

        return return_map;
    }
}
