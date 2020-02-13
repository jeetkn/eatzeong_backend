package com.place.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.print.attribute.standard.Media;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.place.dto.AppReviewDto;
import com.place.dto.Dto;
import com.place.service.AppReviewService;
import com.place.util.DayCheck;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
@Slf4j
public class AppReviewController {
	
	@Resource(name="com.place.service.AppReviewService")
	AppReviewService appReview;

	@Value("${image.review}")
	private String image_directory;

	/**
	 * 리뷰 전체 조회
	 */
	@GetMapping("/places/{place_id}/reviews")
	public Dto<List<Map<String, Object>>> selectReviews(
			@PathVariable String place_id,
			AppReviewDto dto){
		Dto<List<Map<String, Object>>> return_dto_list = new Dto<List<Map<String, Object>>>();
		
		try {

			dto.setPlace_id(place_id);
			return_dto_list.setDataList(appReview.selectAppReviewList(dto));
			
		} catch (Exception e) {
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());
			
			e.printStackTrace();
			return_dto_list.setCode(500);
			return_dto_list.setMessage("서버 오류");
			return_dto_list.setDataList(Lists.newArrayList(error));
			return return_dto_list;
		}
		
		return return_dto_list;
	}

	@GetMapping("/myreviews")
	public Dto<List<Map<String, Object>>> selectMyReviews(AppReviewDto review_dto){
		Dto<List<Map<String, Object>>> return_dto = new Dto<>();

		try{

			return_dto.setDataList(appReview.selectMyReviewList(review_dto));

		}catch (Exception e){
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Lists.newArrayList(error));
			return return_dto;
		}

		return return_dto;
	}

	/**
	 * 리뷰 삽입
	 */
	@PostMapping(value="/places/{place_id}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Dto<Map<String, Object>> insertReview(@RequestPart(name = "file", required=false) ArrayList<FilePart> files,
									 @PathVariable String place_id,
									 AppReviewDto app_review_dto) throws IOException {

		Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();

		List<String> filename_list = new ArrayList<>();

		try {
			app_review_dto.setPlace_id(place_id);
			app_review_dto.setReview_contents(URLDecoder.decode(app_review_dto.getReview_contents(), "UTF-8"));

			log.info("place_id : {}", place_id);
			log.info("app_review_dto : {}", app_review_dto.toString());
			log.info("image stored directory : {}", System.getProperty("user.dir")+ image_directory);



			if(app_review_dto.getReview_user_id() == null || app_review_dto.getReview_user_id().isBlank())
				throw new Exception("review_user_id 파라미터를 확인해주세요.");
			if(app_review_dto.getReview_contents() == null || app_review_dto.getReview_contents().isBlank())
				throw new Exception("review_contents 파라미터를 확인해주세요.");
			if(app_review_dto.getRating_point() == 0)
				throw new Exception("rating_point 파라미터를 확인해주세요. 0이면 안됩니다.");

			appReview.insertAppReview(app_review_dto, files);
//			return_dto.setDataList(appReview.selectAppReview(app_review_dto));

		} catch (Exception e) {
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
	 *	리뷰 수정 
	 */
	@PutMapping(value = "/places/{place_id}/reviews/{review_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Dto<Map<String, Object>> updateReview(
			@PathVariable String place_id,
			@PathVariable String review_id,
			@RequestPart(name = "file", required=false) ArrayList<FilePart> files,
			AppReviewDto app_review_dto) {
		
		Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();
		
		try {
			if(app_review_dto.getReview_user_id() == null || app_review_dto.getReview_user_id().isBlank())
				throw new Exception("review_user_id 파라미터를 확인해주세요.");
			if(app_review_dto.getReview_contents() == null || app_review_dto.getReview_contents().isBlank())
				throw new Exception("review_contents 파라미터를 확인해주세요.");
			if(app_review_dto.getRating_point() == 0)
				throw new Exception("rating_point 파라미터를 확인해주세요. 0이면 안됩니다.");

			app_review_dto.setPlace_id(place_id);
			app_review_dto.setReview_id(review_id);
			app_review_dto.setUser_id(app_review_dto.getReview_user_id());
			app_review_dto.setReview_contents(URLDecoder.decode(app_review_dto.getReview_contents(), "UTF-8"));
			appReview.reviewUpdate(app_review_dto, files);
//			return_dto.setDataList(appReview.selectAppReview(app_review_dto));
			
		} catch (Exception e) {
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
	 * 리뷰 삭제
	 */
	@DeleteMapping(value = "/places/{place_id}/reviews/{review_id}")
	public Dto<Map<String, Object>> deleteReview(
			AppReviewDto app_review_dto){
		
		Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();
		Map<String, Object> return_map = Maps.newHashMap();
		
		try {
			if(app_review_dto.getReview_user_id() == null || app_review_dto.getReview_user_id().isBlank())
				throw new Exception("review_user_id 파라미터를 확인해주세요.");
			
			appReview.deleteAppReview(app_review_dto);
			
			return_map.put("result_message", "리뷰 삭제 성공");
			return_dto.setDataList(return_map);
			
		} catch (Exception e) {
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

}
