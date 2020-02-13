package com.place.repository;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.place.dto.MainDto;
import com.place.dto.PlaceDto;
import org.springframework.stereotype.Repository;

import com.place.service.mapper.CommonMapper;


@Repository
public class CommonRepository {

	
	@Resource(name="com.place.service.mapper.CommonMapper")
	CommonMapper mapper;

	
	// 지역 검색
	public List<Map<String, String>> selectArea() throws Exception {
		return mapper.selectArea();
	}
	
	// 추천 검색어
	public List<String> selectSuggestKeyword(String keyword) throws Exception {
		return mapper.selectSuggestKeyword(keyword);
	}

	public int selectBookmarkCnt(Map<String, String> allRequestParams) throws Exception {
		if(allRequestParams.get("gubun").equals("place"))
			return mapper.selectBookmarkPlaceCount(allRequestParams);
		else
			return  mapper.selectBookmarkReviewBlogCount(allRequestParams);
	}
	
	public List<Map<String, Object>> selectBookmarks(Map<String, String> allRequestParams) throws Exception {
		if(allRequestParams.get("gubun").equals("youtube"))
			return mapper.selectBookmarkPortalReviews(allRequestParams);
		else if(allRequestParams.get("gubun").equals("naver") || allRequestParams.get("gubun").equals("tistory"))
			return mapper.selectBookmarkPortalBlogs(allRequestParams);
		else if(allRequestParams.get("gubun").equals("app"))
			return mapper.selectBookmarkAppReviews(allRequestParams);
		else
			return mapper.selectBookmarkPlaces(allRequestParams);
	}

	public void insertBookmarks(Map<String, String> allRequestParams) throws Exception {
		if(allRequestParams.get("gubun").equals("place"))
			mapper.insertBookmarkPlaces(allRequestParams);
		else
			mapper.insertBookmarkReviewBlog(allRequestParams);
	}

	public void deleteBookmarks(Map<String, String> allRequestParams) throws Exception {
		if(allRequestParams.get("gubun").equals("place"))
			mapper.updateBookmarkPlaces(allRequestParams);
		else
			mapper.updateBookmarkReviewBlog(allRequestParams);
	}

    public int selectMainCount(Map<String, String> request_param) throws Exception {
		return mapper.selectMainCount(request_param);
    }

    public void insertCustomSearch(List<MainDto> main_dto_list){
		main_dto_list.stream()
				.forEach(dto -> {
					int count = mapper.selectCustomSearchCount(dto);
					if(count < 1)
						mapper.insertCustomSearch(dto);
				});
    }

	public List<MainDto> selectMain(Map<String, String> request_param) throws Exception {
		return mapper.selectMain(request_param);
	}

	public List<Map<String, String>> selectFirstArea() throws Exception{
		return mapper.selectFirstArea();
	}

	public List<Map<String, String>> selectSecondArea(String area) throws Exception{
		return mapper.selectSecondArea(area);
	}

	public List<Object> selectSuggestArea() throws Exception{
		return mapper.selectSuggestArea();
	}

	public int selectMainPlacesCount(PlaceDto place_dto) {
		return mapper.selectMainPlacesCount(place_dto);
	}

	public List<Map<String, Object>> selectMainPlaces(PlaceDto place_dto) {
		return mapper.selectMainPlaces(place_dto);
	}
}