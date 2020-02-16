package com.place.controller;

import com.google.common.collect.Maps;
import com.place.dto.AppReviewDto;
import com.place.dto.Dto;
import com.place.dto.UserDto;
import com.place.exception.ExistException;
import com.place.service.PlaceService;
import com.place.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.InvalidParameterException;
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
     * 일반사용자 회원가입
     * @param userDto
     * @return
     */
    @PostMapping(value = "users/general/signup")
    public Dto<Map<String, Object>> generalSignUp(UserDto userDto){
        Dto<Map<String, Object>> return_dto = new Dto<>();

        try {
            if (userDto.getEmail() == null || userDto.getEmail().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getPassword() == null || userDto.getPassword().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getGender() == null || userDto.getGender().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getNickname() == null || userDto.getNickname().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getBirthday() == null || userDto.getBirthday().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getPhone_number() == null || userDto.getPhone_number().isBlank())
                throw new InvalidParameterException("Invalid parameter");

            userService.insertGeneralUser(userDto);

        }catch (ExistException e){
            e.printStackTrace();
            return_dto.setCode(409);
            return_dto.setMessage("Already exist data");
            return return_dto;
        } catch (InvalidParameterException e){
            e.printStackTrace();
            return_dto.setCode(400);
            return_dto.setMessage("Invalid parameter");
            return return_dto;
        } catch (Exception e) {
            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
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
            return_dto.setMessage("server error");
            return_dto.setDataList(error);
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 아이디 찾기
     * @param userDto
     * @return
     */
    @GetMapping("/users/general/findid")
    public Dto<List<String>> findId(UserDto userDto){
        Dto<List<String>> return_dto = new Dto<>();

        try{
            if (userDto.getPhone_number() == null || userDto.getPhone_number().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getBirthday() == null || userDto.getBirthday().isBlank())
                throw new InvalidParameterException("Invalid parameter");

            return_dto.setDataList(userService.findId(userDto));

        } catch (InvalidParameterException e){
            e.printStackTrace();
            return_dto.setCode(400);
            return_dto.setMessage("Invalid parameter");
            return return_dto;
        } catch (Exception e) {
            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 계정 여부 체크
     * @param userDto
     * @return
     */
    @GetMapping("/users/general/accountcheck")
    public Dto<Map<String,Object>> accountCheck(UserDto userDto){
        Dto<Map<String,Object>> return_dto = new Dto<>();

        try{
            if (userDto.getPhone_number() == null || userDto.getPhone_number().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getEmail() == null || userDto.getEmail().isBlank())
                throw new InvalidParameterException("Invalid parameter");

            return_dto.setDataList(userService.finePassword(userDto));

        } catch (InvalidParameterException e){
            e.printStackTrace();
            return_dto.setCode(400);
            return_dto.setMessage("Invalid parameter");
            return return_dto;
        } catch (Exception e) {
            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 일반사용자 탈퇴하기
     * @param userDto
     * @return
     */
    @PutMapping("/users/general/accountclose")
    public Dto<Map<String, Object>> accountClose(UserDto userDto){
        Dto<Map<String,Object>> return_dto = new Dto<>();

        try{
            if (userDto.getUser_id() == null || userDto.getUser_id().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getPassword() == null || userDto.getPassword().isBlank())
                throw new InvalidParameterException("Invalid parameter");

            userDto.setSns_division("C");
            userDto.setEmail(userDto.getUser_id());
            return_dto.setDataList(userService.accountClose(userDto));

        } catch (InvalidParameterException e){
            e.printStackTrace();
            return_dto.setCode(400);
            return_dto.setMessage("Invalid parameter");
            return return_dto;
        } catch (Exception e) {
            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
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
            return_map.put("result_flag", true);
            return_map.put("result_message", "비밀번호 변경 성공");
            return_dto.setDataList(return_map);

        }catch (Exception e){
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dto.setDataList(error);
            return return_dto;
        }

        return return_dto;
    }

    @PutMapping(value = "/users/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Dto<Map<String, Object>> updateProfileImage(@RequestPart(name = "file", required=false) FilePart file,
                                                       UserDto userDto) throws IOException {
        Dto<Map<String, Object>> return_dto = new Dto<>();

        try{
            if (userDto.getUser_id() == null || userDto.getUser_id().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getSns_division() == null || userDto.getSns_division().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (file == null)
                throw new InvalidParameterException("Invalid parameter");

            return_dto.setDataList(userService.updateProfile(userDto, file));

        } catch (InvalidParameterException e){
            e.printStackTrace();
            return_dto.setCode(400);
            return_dto.setMessage("Invalid parameter");
            return return_dto;
        } catch (Exception e) {
            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
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
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    @GetMapping("/users/user")
    public Dto<Map<String, Object>> selectUserInfo(UserDto userDto){
        Dto<Map<String, Object>> return_dto = new Dto<>();

        try{
            if (userDto.getUser_id() == null || userDto.getUser_id().isBlank())
                throw new InvalidParameterException("Invalid parameter");
            if (userDto.getSns_division() == null || userDto.getSns_division().isBlank())
                throw new InvalidParameterException("Invalid parameter");

            return_dto.setDataList(userService.selectUserInfo(userDto));

        } catch (InvalidParameterException e){
            e.printStackTrace();
            return_dto.setCode(400);
            return_dto.setMessage("Invalid parameter");
            return return_dto;
        } catch (Exception e) {
            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
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
            return_dto.setMessage("server error");
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
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }
}
