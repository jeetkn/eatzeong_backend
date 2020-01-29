package com.place.service.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.place.dto.AppReviewDto;
import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.dto.PortalBlogDto;

/* 
 * 
 * 맵퍼에 관한 인터페이스 정의 해 주는 곳임.
 * 각 메소드명은 맵퍼 id 와 동일하며 resultType 로 
 * 메소드의 타입을 정한다.
 * 
 * */
@Repository("com.place.service.mapper.BlogMapper") //--> 맵퍼위치 세팅

public interface BlogMapper {
	
	public List<PortalBlogDto> selectBlog(PlaceDetailDto place_dto) throws Exception;
	
	public List<PortalBlogDto> selectPortalBlog(PortalBlogDto blog_dto) throws Exception;
	
	public int selectBlogCount(List<PortalBlogDto> blog_list) throws Exception;
	
	public int insertBlog(PortalBlogDto blog_list) throws Exception;
	
}
