package com.place.service.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;

/* 
 * 
 * 맵퍼에 관한 인터페이스 정의 해 주는 곳임.
 * 각 메소드명은 맵퍼 id 와 동일하며 resultType 로 
 * 메소드의 타입을 정한다.
 * 
 * */
@Repository("com.place.service.mapper.PlaceMapper") //--> 맵퍼위치 세팅

public interface PlaceMapper {
	
	public List<PlaceDto> selectPlaceList(PlaceDto dto) throws Exception;
	
	public int selectPlaceCount(PlaceDto dto) throws Exception;
	
	public int insertPlace(PlaceDto dto) throws Exception;
	
	public List<PlaceDetailDto> selectPlaceDetail(PlaceDetailDto dto) throws Exception;
	
	public void updatePlace(PlaceDetailDto dto) throws Exception;

	void insertKeyword(PlaceDto dto) throws Exception;

	List<Map<String, Object>> getPopularSearches() throws Exception;

    int selectNaverBlogCount(Map<String, String> result_map) throws Exception;

	void insertNaverBlog(Map<String, String> result_map) throws Exception;

	int selectNaverBlogsCount(Map<String, String> fields_map) throws Exception;

	int selectDaumBlogsCount(Map<String, String> fields_map) throws Exception;

	List<Map<String, Object>> selectNaverBlogs(Map<String, String> fields_map) throws Exception;

	int selectDaumBlogCount(Map<String, String> temp_map) throws Exception;

	void insertDaumBlog(Map<String, String> temp_map) throws Exception;

	List<Map<String, Object>> selectDaumBlogs(Map<String, String> fields_map) throws Exception;

	int selectYoutubeReviewsCount(Map<String, String> fields_map) throws Exception;

	int selectYoutubeReviewCount(Map<String, String> temp_map) throws Exception;

	void insertYoutubeReview(Map<String, String> temp_map) throws Exception;

	List<Map<String, Object>> selectYoutubeReviews(Map<String, String> fields_map) throws Exception;

	int selectGoogleReviewsCount(Map<String, String> fields_map) throws Exception;

	int selectGoogleReviewCount(Map<String, String> temp_map) throws Exception;

	void insertGoogleReview(Map<String, String> temp_map) throws Exception;

	List<Map<String, Object>> selectGoogleReviews(Map<String, String> fields_map) throws Exception;
}
