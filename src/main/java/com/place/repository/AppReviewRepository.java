package com.place.repository;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.place.dto.AppReviewDto;
import com.place.service.mapper.CommonMapper;
import com.place.service.mapper.ReviewMapper;

@Repository
public class AppReviewRepository {

	@Resource(name="com.place.service.mapper.ReviewMapper")
	ReviewMapper mapper;

	/**
	 * 리뷰 count 조회
	 * 
	 * @param app_review_dto
	 * @return
	 */
	public int selectAppReviewCount(AppReviewDto app_review_dto) throws Exception{
		return mapper.selectAppReviewCount(app_review_dto);
	}
	
	/**
	 * 리뷰 단일 검색
	 * 
	 * @param app_review_dto
	 * @return
	 */
	public List<AppReviewDto> selectAppReview(AppReviewDto app_review_dto) throws Exception {
		return mapper.selectAppReview(app_review_dto);
	}
	
	/**
	 * 리뷰 전체 검색
	 * 
	 * @param app_review_dto
	 * @return
	 */
	public List<AppReviewDto> selectAppReviewList(AppReviewDto app_review_dto) throws Exception{
		return mapper.selectAppReviewList(app_review_dto);
	}
	
	/**
	 * 리뷰 삽입
	 * 
	 * @param app_review_dto
	 */
	public void insertAppReview(AppReviewDto app_review_dto) throws Exception {
		mapper.insertAppReview(app_review_dto);
	}
	
	/**
	 * 첨부파일 삽입
	 * 
	 * @param attachment_list
	 */
	public void insertAppReviewAttachment(List<Map<String, String>> attachment_list) throws Exception {
		for(Map<String, String> attachment_map : attachment_list) {
			mapper.insertAppReviewAttachment(attachment_map);
		}
	}

	public void updateAppReview(AppReviewDto app_review_dto) throws Exception {
		mapper.updateAppReview(app_review_dto);
	}

	public void deleteAppReview(AppReviewDto app_review_dto) throws Exception  {
		mapper.deleteAppReview(app_review_dto);
	}

    public List<AppReviewDto> selectMyReviewList(AppReviewDto review_dto) throws Exception{
		return mapper.selectMyReviewList(review_dto);
    }

    public int selectLikeReview(AppReviewDto app_review_dto) throws Exception{
		return mapper.selectLikeReview(app_review_dto);
    }

	public void insertLikeReview(AppReviewDto app_review_dto) throws Exception{
		mapper.insertLikeReview(app_review_dto);
	}

	public void deleteLikeReview(AppReviewDto app_review_dto) throws Exception{
		mapper.deleteLikeReview(app_review_dto);
	}
}
