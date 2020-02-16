package com.place.service.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.place.dto.AppReviewDto;
import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.dto.PortalReviewDto;

/* 
 * 
 * 맵퍼에 관한 인터페이스 정의 해 주는 곳임.
 * 각 메소드명은 맵퍼 id 와 동일하며 resultType 로 
 * 메소드의 타입을 정한다.
 * 
 * */
@Repository("com.place.service.mapper.ReviewMapper") //--> 맵퍼위치 세팅

public interface ReviewMapper {
	
	public int selectAppReviewCount(AppReviewDto app_review_dto) throws Exception;
	
	public int insertPlace(PlaceDto dto) throws Exception;
	
	public List<AppReviewDto> selectAppReview(AppReviewDto app_review_dto) throws Exception;
	
	public List<AppReviewDto> selectAppReviewList(AppReviewDto app_review_dto) throws Exception;
	
	public void insertAppReview(AppReviewDto app_review_dto) throws Exception;
	
	public void insertAppReviewAttachment(Map<String, String> attachment_map) throws Exception;
	
	public void updateAppReview(AppReviewDto app_review_dto) throws Exception;
	
	public void deleteAppReview(AppReviewDto app_review_dto) throws Exception;
	
	
	public List<PortalReviewDto> selectReviews(PlaceDetailDto place_dto) throws Exception;
	
	public List<PortalReviewDto> selectPortalReview(PortalReviewDto review_dto) throws Exception;
	
	public int selectPortalReviewCount(List<PortalReviewDto> review_list) throws Exception;
	
	public int insertReview(List<PortalReviewDto> review_list) throws Exception;

    public List<AppReviewDto> selectMyReviewList(AppReviewDto review_dto) throws Exception;

    int selectLikeReview(AppReviewDto app_review_dto) throws Exception;

	void insertLikeReview(AppReviewDto app_review_dto) throws Exception;

	void deleteLikeReview(AppReviewDto app_review_dto) throws Exception;
}
