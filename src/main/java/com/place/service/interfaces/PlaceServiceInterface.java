package com.place.service.interfaces;

import java.util.List;
import java.util.Map;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.dto.PortalBlogDto;
import com.place.dto.PortalReviewDto;

public interface PlaceServiceInterface {

	/**
	 * 장소 목록 조회
	 * 
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	List<Object> selectPlaceList(PlaceDto dto) throws Exception;
	
	/**
	 * 장소 디테일 정보 조회
	 * 
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> selectPlaceDetail(PlaceDetailDto dto) throws Exception;

	/**
	 * 포탈 리뷰 조회(유튜브, 구글)
	 * 
	 * @param place_dto
	 * @param parameter 
	 * @return
	 */
/*	Map<String, List<PortalReviewDto>> selectPlacePortalReview(PlaceDetailDto place_dto, Map<String, String> parameter);*/

	/**
	 * 포털 블로그 조회(네이버, 다음)
	 * 
	 * @param place_dto
	 * @return
	 */
/*	Map<String, List<PortalBlogDto>> selectPlacePortalBlog(PlaceDetailDto place_dto);

    Map<String, Object> selectPlacePortalReviews(Map<String, String> parameter) throws Exception;*/

    List<Object> getPopularSearches() throws Exception;

	List<Map<String, Object>> selectNaverBlog(Map<String, String> request_param) throws Exception;

    List<Map<String, Object>> selectDaumBlog(Map<String, String> request_param) throws Exception;

    List<Map<String, Object>> selectYoutubeReview(Map<String, String> request_param) throws Exception;

    List<Map<String, Object>> selectGoogleReview(Map<String, String> request_param) throws Exception;
}
