package com.place.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.place.dto.Dto;
import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.dto.PortalBlogDto;
import com.place.dto.PortalReviewDto;
import com.place.service.PlaceService;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
public class PlaceController {
	
	@Resource(name="com.place.service.PlaceService")
	PlaceService placeservice;
	
	/**
	 * 장소 목록 조회
	 */
	@GetMapping(value = "/places")
	public Dto<List<PlaceDto>> selectPlace(@RequestParam("q") String keyword, PlaceDto dto) {
		Dto<List<PlaceDto>> list_dto = new Dto<List<PlaceDto>>();
		try {
			if(keyword.isBlank() || keyword==null) {
				list_dto.setCode(400);
				list_dto.setMessage("필수 파라미터를 확인해주세요.");
				return list_dto;
			}
			dto.setKeyword(keyword);
			list_dto = placeservice.selectPlaceList(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list_dto;
	}
	
	/**
	 * 장소 상세 조회
	 * @param place_id
	 * @param detail_dto
	 */
	@GetMapping(value = "/places/{place_id}")
	public Dto<PlaceDetailDto> selectPlaceDetail(@PathVariable String place_id, PlaceDetailDto detail_dto) {
		Dto<PlaceDetailDto> return_dto = new Dto<PlaceDetailDto>();
		try {
			if(detail_dto.getLatitude() == null || detail_dto.getLongitude() == null || detail_dto.getLatitude().isBlank() || detail_dto.getLongitude().isBlank()) {
				return_dto.setCode(400);
				return_dto.setMessage("필수 파라미터를 확인해주세요.");
				return return_dto;
			}
			detail_dto.setPlace_id(place_id);
			return_dto = placeservice.selectPlaceDetail(detail_dto);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return return_dto;
	}
	
	/**
	 * 포털리뷰 조회(google, youtube)
	 * @param place_id
	 */
	@GetMapping(value = "/places/{place_id}/portalreviews")
	public Dto<Map<String,  List<PortalReviewDto>>> selectPortalReviews(
			@PathVariable String place_id, 
			@RequestParam(name = "page", defaultValue = "0") String start_page,
			@RequestParam(name = "portal", defaultValue = "ALL") String portal) {
		
		Dto<Map<String,  List<PortalReviewDto>>> return_dto = new Dto<Map<String, List<PortalReviewDto>>>();
		PlaceDetailDto place_dto = new PlaceDetailDto();
		Map<String, List<PortalReviewDto>> review_dto = Maps.newHashMap();
		Map<String, String> parameter = Maps.newHashMap();
		
		if(place_id == null || place_id.isBlank()) {
			return_dto.setCode(400);
			return_dto.setMessage("필수 파라미터를 확인해주세요.");
			return return_dto;
		}
		if (!NumberUtils.isCreatable(start_page)) {
			return_dto.setCode(400);
			return_dto.setMessage("page 파라미터를 확인해주세요.");
			return return_dto;
		}
		if (Integer.parseInt(start_page) < 0) {
			return_dto.setCode(400);
			return_dto.setMessage("page 파라미터를 확인해주세요.");
			return return_dto;
		}		
		if(!(portal.equalsIgnoreCase("YOUTUBE")  || portal.equalsIgnoreCase("ALL"))) {
			return_dto.setCode(400);
			return_dto.setMessage("portal 파라미터를 확인해주세요.");
			return return_dto;
		}
		
		
		try {
			parameter.put("portal", portal.toUpperCase());
			parameter.put("start_page", start_page);
			
			place_dto.setPlace_id(place_id);
			place_dto = placeservice.selectPlaceDetail(place_dto).getDataList();
			
			review_dto = placeservice.selectPlacePortalReview(place_dto, parameter);
			return_dto.setDataList(review_dto);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return return_dto;
	}
	
	/**
	 * 블로그 조회(naver, tistory)
	 * @param place_id
	 */
	@GetMapping(value = "/places/{place_id}/portalblogs")
	public Dto<Map<String,List<PortalBlogDto>>> selectPortalBlog(			
			@PathVariable String place_id, 
			@RequestParam(name = "page", defaultValue = "0") String start_page,
			@RequestParam(name = "portal", defaultValue = "ALL") String portal) {
		
		Dto<Map<String,List<PortalBlogDto>>> return_dto = new Dto<Map<String,List<PortalBlogDto>>>();
		PlaceDetailDto place_dto = new PlaceDetailDto();
		Map<String, List<PortalBlogDto>> blog_dto = Maps.newHashMap();
		Map<String, String> parameter = Maps.newHashMap();
		
		if(place_id == null || place_id.isBlank()) {
			return_dto.setCode(400);
			return_dto.setMessage("필수 파라미터를 확인해주세요.");
			return return_dto;
		}
		if (!NumberUtils.isCreatable(start_page)) {
			return_dto.setCode(400);
			return_dto.setMessage("page 파라미터를 확인해주세요.");
			return return_dto;
		}
		if (Integer.parseInt(start_page) < 0) {
			return_dto.setCode(400);
			return_dto.setMessage("page 파라미터를 확인해주세요.");
			return return_dto;
		}		
		if(!(portal.equalsIgnoreCase("NAVER") || portal.equalsIgnoreCase("DAUM") || portal.equalsIgnoreCase("ALL"))) {
			return_dto.setCode(400);
			return_dto.setMessage("portal 파라미터를 확인해주세요.");
			return return_dto;
		}
		
		try {
			parameter.put("portal", portal.toUpperCase());
			parameter.put("start_page", start_page);

			place_dto.setPlace_id(place_id);
			place_dto = placeservice.selectPlaceDetail(place_dto).getDataList();
			
			blog_dto = placeservice.selectPlacePortalBlog(place_dto);
			return_dto.setDataList(blog_dto);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return return_dto;
	}
	
}
