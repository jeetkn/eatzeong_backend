package com.place.service.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.place.dto.AppReviewDto;
import com.place.dto.Dto;
import com.place.dto.PlaceDto;

public interface AppReviewServiceInterface {

	Map<String, Object> selectAppReview(AppReviewDto app_review_dto) throws Exception;
	
	List<Map<String, Object>> selectAppReviewList(String place_id) throws Exception;
	
	void insertAppReview(AppReviewDto app_review_dto, ArrayList<MultipartFile> files) throws Exception;

	void reviewUpdate(AppReviewDto app_review_dto, ArrayList<MultipartFile> files)  throws Exception;

	void deleteAppReview(AppReviewDto app_review_dto) throws Exception;

}
