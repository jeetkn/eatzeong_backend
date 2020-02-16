package com.place.service.mapper;

import java.util.List;
import java.util.Map;

import com.place.dto.MainDto;
import com.place.dto.PlaceDto;
import org.springframework.stereotype.Repository;

/* 
 * 
 * 맵퍼에 관한 인터페이스 정의 해 주는 곳임.
 * 각 메소드명은 맵퍼 id 와 동일하며 resultType 로 
 * 메소드의 타입을 정한다.
 * 
 * */
@Repository("com.place.service.mapper.CommonMapper") //--> 맵퍼위치 세팅
public interface CommonMapper {
	
	public List<Map<String, String>> selectArea() throws Exception;
	
	public List<String> selectSuggestKeyword(String keyword) throws Exception;

	public List<Map<String, Object>> selectBookmarkPlaces(Map<String, String> allRequestParams) throws Exception;
	
	public List<Map<String, Object>> selectBookmarkPortalReviews(Map<String, String> allRequestParams) throws Exception;
	
	public List<Map<String, Object>> selectBookmarkPortalBlogs(Map<String, String> allRequestParams) throws Exception;

	public List<Map<String, Object>> selectBookmarkAppReviews(Map<String, String> allRequestParams) throws Exception;

	public void insertBookmarkPlaces(Map<String, String> allRequestParams) throws Exception;
	
	public void insertBookmarkReviewBlog(Map<String, String> allRequestParams) throws Exception;
	
	public void updateBookmarkPlaces(Map<String, String> allRequestParams) throws Exception;
	
	public void updateBookmarkReviewBlog(Map<String, String> allRequestParams) throws Exception;
	
	public int selectBookmarkReviewBlogCount(Map<String, String> allRequestParams) throws Exception;
	
	public int selectBookmarkPlaceCount(Map<String, String> allRequestParams) throws Exception;

	public int selectMainCount(Map<String, String> request_param) throws Exception;

    public int selectCustomSearchCount(MainDto dto);

	public void insertCustomSearch(MainDto main_dto_list);

	public List<MainDto> selectMain(Map<String, String> request_param) throws Exception;

    List<Map<String, String>> selectFirstArea() throws Exception;

	List<Map<String, String>> selectSecondArea(String area) throws Exception;

	List<Object> selectSuggestArea() throws Exception;

    int selectMainPlacesCount(PlaceDto place_dto);

	List<Map<String, Object>> selectMainPlaces(PlaceDto place_dto);

	int selectBookmarkFlag(Map<String, String> allRequestParams) throws Exception;
}
