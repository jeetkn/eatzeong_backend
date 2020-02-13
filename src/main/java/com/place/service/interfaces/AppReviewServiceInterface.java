package com.place.service.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;

import com.place.dto.AppReviewDto;

public interface AppReviewServiceInterface {

	Map<String, Object> selectAppReview(AppReviewDto app_review_dto) throws Exception;
	
	List<Map<String, Object>> selectAppReviewList(AppReviewDto dto) throws Exception;
	
	void insertAppReview(AppReviewDto app_review_dto, ArrayList<FilePart> files) throws Exception;

	void reviewUpdate(AppReviewDto app_review_dto, ArrayList<FilePart> files)  throws Exception;

	void deleteAppReview(AppReviewDto app_review_dto) throws Exception;

    List<Map<String, Object>> selectMyReviewList(AppReviewDto review_dto) throws Exception;
}
