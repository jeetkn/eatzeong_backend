package com.place.repository;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
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
}