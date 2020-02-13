package com.place.controller;

import com.google.common.collect.Maps;
import com.place.dto.Dto;
import com.place.dto.UserDto;
import com.place.service.PlaceService;
import com.place.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
@Slf4j
public class UserController {

    @Resource(name="com.place.service.UserService")
    UserService userService;

    /**
     * 일반사용자 이메일체크
     * @param userDto
     * @return
     */
    @GetMapping(value = "users/general/checkemail")
    public Dto<Map<String, Object>> checkEmail(UserDto userDto){
        Dto<Map<String, Object>> return_dto = new Dto<>();

        try {
            Map<String, Object> return_map = new HashMap<>();
            userDto.setSns_division("C");
            if(userService.getGeneralEmailCheck(userDto)){
                return_map.put("result", true);
                return_map.put("result_message", "사용 가능한 이메일입니다.");
            }else{
                return_map.put("result", false);
                return_map.put("result_message", "이미 가입된 이메일입니다.");
            }
            return_dto.setDataList(return_map);

        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(error);
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 일반사용자 회원가입
     * @param userDto
     * @return
     */
    @PostMapping(value = "users/general/signup")
    public Dto<Map<String, Object>> generalSignUp(UserDto userDto){
        Dto<Map<String, Object>> return_dto = new Dto<>();

        try {
            Map<String, Object> return_map = new HashMap<>();
            userService.insertGeneralUser(userDto);
            return_map.put("result_message", "성공하였습니다.");
            return_dto.setDataList(return_map);

        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(error);
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 일반사용자 로그인
     * @param userDto
     * @param user_id
     * @return
     */
    @GetMapping(value = "/users/general/signin")
    public Dto<Map<String, Object>> generalSignIn(UserDto userDto, @RequestParam String user_id){
        Dto<Map<String, Object>> return_dto = new Dto<>();

        try{
            if("".equals(user_id.trim())){
                Map<String, Object> temp_map = new LinkedHashMap<>();
                temp_map.put("result", false);
                temp_map.put("result_message", "user_id를 확인해주세요.");
                return_dto.setDataList(temp_map);
                return return_dto;
            }

            Map<String, Object> return_map = new HashMap<>();
            userDto.setEmail(user_id);
            userDto.setSns_division("C");
            return_dto.setDataList(userService.generalSignIn(userDto));

        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(error);
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 일반사용자 비밀번호 수정
     * @param userDto
     * @return
     */
    @PutMapping(value = "/users/general/password")
    public Dto<Map<String, Object>> updatePassword(UserDto userDto){
        Dto<Map<String, Object>> return_dto = new Dto<>();
        Map<String, Object> return_map = new HashMap<>();

        try{
            userService.updatePassword(userDto);
            return_map.put("result", "SUCCESS");
            return_dto.setDataList(return_map);

        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(error);
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 공지사항 조회
     * @return
     */
    @GetMapping(value = "/users/notice")
    public Dto<List<Object>> getNotice(){
        Dto<List<Object>> return_dto = new Dto<>();
        Map<String, Object> return_map = new HashMap<>();

        try{
            return_dto.setDataList(userService.selectNotice());
        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    @GetMapping(value = "/users/terms")
    public Dto<List<Object>> getTerms(){
        Dto<List<Object>> return_dto = new Dto<>();
        Map<String, Object> return_map = new HashMap<>();

        try{
            return_dto.setDataList(userService.selectTerms());
        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    @GetMapping(value = "/users/faqs")
    public Dto<List<Object>> getFaqs(){
        Dto<List<Object>> return_dto = new Dto<>();
        Map<String, Object> return_map = new HashMap<>();

        try{
            return_dto.setDataList(userService.selectFaqs());
        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("서버 오류");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }
}
