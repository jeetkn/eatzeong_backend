package com.place.service;

import ch.qos.logback.core.util.FileSize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.place.dto.AppReviewDto;
import com.place.dto.UserDto;
import com.place.exception.ExistException;
import com.place.repository.UserRepository;
import com.place.service.interfaces.UserServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("com.place.service.UserService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
@Slf4j
public class UserService implements UserServiceInterface {

    @Inject
    UserRepository user;

    @Value("${image.profile}")
    private String PROFILE_DIRECTORY;

    @Value("${host.url}")
    private String HOST_URL;

    @Override
    public Map<String, Object> insertGeneralUser(UserDto userDto) throws Exception{
        Map<String, Object> return_map = new LinkedHashMap<>();

        userDto.setSns_division("C");

        int count = user.selectGeneralUser(userDto);
        if(count < 1) {
            user.insertGeneralUser(userDto);
        }else{
            throw new ExistException();
        }

        return return_map;
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
                    Map<String, Object> temp_map = new LinkedHashMap<>();
                    Map<String, Object> contents_map = new HashMap<>();
                    List<Map<String, Object>> contents_list = new ArrayList<>();
                    temp_map.put("notice_id", res.get("notice_id"));
                    temp_map.put("add_date", res.get("add_date"));
                    temp_map.put("add_time", res.get("add_time"));
                    temp_map.put("user_id", res.get("user_id"));
                    temp_map.put("subject", res.get("notice_subject"));
                    contents_map.put("contents", res.get("notice_contents"));
                    contents_list.add(contents_map);
                    temp_map.put("items", contents_list);
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
                    Map<String, Object> temp_map = new LinkedHashMap<>();
                    Map<String, Object> contents_map = new HashMap<>();
                    List<Map<String, Object>> contents_list = new ArrayList<>();
                    temp_map.put("terms_id", res.get("terms_id").toString());
                    temp_map.put("terms_division", res.get("terms_division").toString());
                    temp_map.put("add_date", res.get("add_date").toString());
                    temp_map.put("add_time", res.get("add_time").toString());
                    temp_map.put("subject", res.get("terms_contents").toString());
                    contents_map.put("contents", res.get("terms_contents"));
                    contents_list.add(contents_map);
                    temp_map.put("items", contents_list);
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
                    Map<String, Object> temp_map = new LinkedHashMap<>();
                    Map<String, Object> contents_map = new HashMap<>();
                    List<Map<String, Object>> contents_list = new ArrayList<>();
                    temp_map.put("faq_id", res.get("faq_id").toString());
                    temp_map.put("add_date", res.get("add_date").toString());
                    temp_map.put("add_time", res.get("add_time").toString());
                    temp_map.put("subject", res.get("faq_subject").toString());
                    contents_map.put("contents", res.get("faq_contents").toString());
                    contents_list.add(contents_map);
                    temp_map.put("items", contents_list);
                    return_map.add(temp_map);
                });

        return return_map;
    }

    @Override
    public List<String> findId(UserDto userDto) throws Exception{
        return user.findId(userDto);
    }

    @Override
    public Map<String, Object> finePassword(UserDto userDto) throws Exception{
        Map<String, Object> result_map = new LinkedHashMap<>();
        Map<String, Object> return_map = new LinkedHashMap<>();

        int count = user.findPassword(userDto);
        if(count > 0) {
            return_map.put("account_flag", true);
            return_map.put("result_message", "계정이 존재합니다.");
        }else{
            return_map.put("account_flag", false);
            return_map.put("result_message", "계정이 존재하지 않습니다.");
        }

        return return_map;
    }

    @Override
    public Map<String, Object> accountClose(UserDto userDto) throws Exception{
        Map<String, Object> return_map = new LinkedHashMap<>();
        String password = user.selectPassword(userDto);
        if(password.equals(userDto.getPassword())) {
            user.accountClose(userDto);
            return_map.put("result_flag", true);
            return_map.put("result_message", "탈퇴 성공");
        }else{
            return_map.put("result_flag", false);
            return_map.put("result_message", "비밀번호가 다릅니다.");
        }
        return return_map;
    }

    @Override
    public Map<String, Object> updateProfile(UserDto userDto, FilePart file) throws Exception{
        Map<String, Object> return_map = new LinkedHashMap<>();

        fileUpload(userDto, file);

        userDto = user.selectUser(userDto);
        return_map.put("user_id", userDto.getUser_id());
        return_map.put("sns_division", userDto.getSns_division());
        return_map.put("profile_image", HOST_URL + "/profile/" + userDto.getProfile_image());

        return return_map;
    }

    @Override
    public Map<String, Object> selectUserInfo(UserDto userDto) throws Exception{
        Map<String, Object> return_map = new LinkedHashMap<>();

        userDto = user.selectUserInfo(userDto);
        return_map.put("user_id", userDto.getUser_id());
        return_map.put("sns_division", userDto.getSns_division());
        return_map.put("nickname", userDto.getNickname());
        return_map.put("phone_number", userDto.getPhone_number());
        if(userDto.getProfile_image() == null || userDto.getProfile_image().isBlank())
            return_map.put("profile_image", null);
        else
            return_map.put("profile_image", HOST_URL + "/profile/" + userDto.getProfile_image());

        return return_map;
    }

    private void fileUpload(UserDto userDto, FilePart file) throws Exception{
        List<Map<String, String>> attachment_list = Lists.newArrayList();

        Map<String, String> attachment_map = Maps.newHashMap();
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random rand = new Random(System.currentTimeMillis());

        String file_name = today + "_" + Math.abs(rand.nextInt(999999)+100000) + "." + FilenameUtils.getExtension(file.filename());

        log.info(file.filename());
        log.info("file : {}", System.getProperty("user.dir")+ PROFILE_DIRECTORY + "/" + file_name);
        File uploadfile = new File(System.getProperty("user.dir")+ PROFILE_DIRECTORY + "/" + file_name);
        file.transferTo(uploadfile);

        attachment_map.put("profile_image_name", file_name);
        attachment_map.put("attach_extension", FilenameUtils.getExtension(file.filename()));
        attachment_map.put("user_id", userDto.getUser_id());
        attachment_map.put("sns_division", userDto.getSns_division());

        user.updateProfileImage(attachment_map);
    }
}
