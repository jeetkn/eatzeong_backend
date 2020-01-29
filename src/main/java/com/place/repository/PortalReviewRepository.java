package com.place.repository;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PortalReviewDto;
import com.place.service.mapper.BlogMapper;
import com.place.service.mapper.ReviewMapper;

@Repository
public class PortalReviewRepository {

	@Resource(name="com.place.service.mapper.ReviewMapper")
	ReviewMapper mapper;
	
	/**
	 * 리뷰 전체 조회
	 * 
	 * @param place_dto
	 * @return
	 */
	public List<PortalReviewDto> selectReviews(PlaceDetailDto place_dto) throws Exception {
		return mapper.selectReviews(place_dto);
	}

	/**
	 * 포털별 리뷰 조회(<strong>유튜브</strong>, <strong>구글</strong>)
	 * 
	 * @param review_dto
	 * @return
	 */
	public List<PortalReviewDto> selectPortalReview(PortalReviewDto review_dto) throws Exception {
		return mapper.selectPortalReview(review_dto);
	}
	
	/**
	 * 리뷰 Insert
	 * 
	 * @param review_list
	 */
	public void insertReview(List<PortalReviewDto> review_list) throws Exception{
		for(PortalReviewDto dto : review_list) {
			int cnt = mapper.selectPortalReviewCount(review_list);
			if(cnt <= 0) {
				mapper.insertReview(review_list);
			}
		}
		
	}

	
}
