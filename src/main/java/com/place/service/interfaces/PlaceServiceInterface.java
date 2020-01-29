package com.place.service.interfaces;

import java.util.List;
import java.util.Map;

import com.place.dto.Dto;
import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.dto.PortalBlogDto;
import com.place.dto.PortalReviewDto;
import com.place.repository.PortalBlogRepository;
import com.place.repository.PortalReviewRepository;

public interface PlaceServiceInterface {

	/**
	 * 장소 목록 조회
	 * 
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	Dto<List<PlaceDto>> selectPlaceList(PlaceDto dto) throws Exception;
	
	/**
	 * 장소 디테일 정보 조회
	 * 
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	Dto<PlaceDetailDto> selectPlaceDetail(PlaceDetailDto dto) throws Exception;

	/**
	 * 포탈 리뷰 조회(유튜브, 구글)
	 * 
	 * @param place_dto
	 * @param parameter 
	 * @return
	 */
	Map<String, List<PortalReviewDto>> selectPlacePortalReview(PlaceDetailDto place_dto, Map<String, String> parameter);

	/**
	 * 포털 블로그 조회(네이버, 다음)
	 * 
	 * @param place_dto
	 * @return
	 */
	Map<String, List<PortalBlogDto>> selectPlacePortalBlog(PlaceDetailDto place_dto);
	
}
