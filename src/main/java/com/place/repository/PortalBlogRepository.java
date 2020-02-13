package com.place.repository;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PortalBlogDto;
import com.place.service.mapper.BlogMapper;
import com.place.service.mapper.PlaceMapper;

@Repository
public class PortalBlogRepository {
	
	@Resource(name="com.place.service.mapper.BlogMapper")
	BlogMapper mapper;
	/**
	 * 블로그 전체 조회
	 * 
	 * @param place_dto
	 * @return
	 */
	public List<PortalBlogDto> selectBlog(PlaceDetailDto place_dto) throws Exception {
		return mapper.selectBlog(place_dto);
	}

	/**
	 * 포털 별 블로그 검색
	 * 
	 * @param blog_dto
	 * @return
	 */
	public List<PortalBlogDto> selectPortalBlog(PortalBlogDto blog_dto) throws Exception {
		List<PortalBlogDto> list = null;
		try {
			list =  mapper.selectPortalBlog(blog_dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 블로그 저장
	 * 
	 * @param blog_list
	 */
	public void insertBlog(List<PortalBlogDto> blog_list) throws Exception{
		for(PortalBlogDto dto : blog_list) {
			int cnt = mapper.selectBlogCount(blog_list);
			if(cnt < 1) {
				mapper.insertBlog(dto); 
			}
		}
	}

}
