package com.place.controller;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.place.dto.AppReviewDto;
import com.place.dto.Dto;
import com.place.service.AppReviewService;
import com.place.util.DayCheck;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
public class AppReviewController {
	
	@Resource(name="com.place.service.AppReviewService")
	AppReviewService appReview;
	
	/**
	 * 리뷰 조회
	 */
	@GetMapping("/places/{place_id}/reviews")
	public Dto<List<Map<String, Object>>> selectReviews(
			@PathVariable String place_id){
		Dto<List<Map<String, Object>>> return_dto_list = new Dto<List<Map<String, Object>>>();
		
		try {
			
			return_dto_list.setDataList(appReview.selectAppReviewList(place_id));
			
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
	
	/**
	 * 리뷰 삽입
	 */
	@PostMapping(value = "/places/{place_id}/reviews", headers = ("content-type=multipart/*"))
	public Dto<Map<String, Object>> insertReview(
			@RequestParam (name="file", required=false) ArrayList<MultipartFile> files,
			AppReviewDto app_review_dto) {
		
		Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();
		
		try {
			System.out.println(app_review_dto);
			if(app_review_dto.getReview_user_id() == null || app_review_dto.getReview_user_id().isBlank())
				throw new Exception("review_user_id 파라미터를 확인해주세요.");
			if(app_review_dto.getReview_contents() == null || app_review_dto.getReview_contents().isBlank())
				throw new Exception("review_contents 파라미터를 확인해주세요.");
			if(app_review_dto.getRating_point() == 0)
				throw new Exception("rating_point 파라미터를 확인해주세요. 0이면 안됩니다.");
			
			appReview.insertAppReview(app_review_dto, files);
			return_dto.setDataList(appReview.selectAppReview(app_review_dto));
			
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
	@PostMapping(value = "/places/{place_id}/reviews/{review_id}", headers = ("content-type=multipart/*"))
	public Dto<Map<String, Object>> updateReview(
			@RequestParam (name="file", required=false) ArrayList<MultipartFile> files,
			AppReviewDto app_review_dto) {
		
		Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();
		
		try {
			if(app_review_dto.getReview_user_id() == null || app_review_dto.getReview_user_id().isBlank())
				throw new Exception("review_user_id 파라미터를 확인해주세요.");
			if(app_review_dto.getReview_contents() == null || app_review_dto.getReview_contents().isBlank())
				throw new Exception("review_contents 파라미터를 확인해주세요.");
			if(app_review_dto.getRating_point() == 0)
				throw new Exception("rating_point 파라미터를 확인해주세요. 0이면 안됩니다.");
			
			appReview.reviewUpdate(app_review_dto, files);
			return_dto.setDataList(appReview.selectAppReview(app_review_dto));
			
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
