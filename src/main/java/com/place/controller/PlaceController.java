package com.place.controller;

import com.google.common.collect.Maps;
import com.place.dto.*;
import com.place.service.PlaceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
@Slf4j
public class PlaceController {
	
	@Resource(name="com.place.service.PlaceService")
	PlaceService placeService;

	/**
	 * 장소 목록 조회
	 */
	@GetMapping(value = "/places")
	public Dto<List<Object>> selectPlace(@RequestParam("q") String keyword, PlaceDto dto) {
		Dto<List<Object>> return_dto = new Dto<>();
		Dto<List<PlaceDto>> list_dto = new Dto<List<PlaceDto>>();

		try {
			if(keyword.isBlank() || keyword==null)
				throw new Exception("필수 파라미터를 확인해주세요.");

			dto.setKeyword(keyword.trim());
			return_dto.setDataList(placeService.selectPlaceList(dto));
		} catch (Exception e) {
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Arrays.asList(error));
			return return_dto;
		}
		return return_dto;
	}

	/**
	 * 장소 상세 조회
	 * @param place_id
	 * @param detail_dto
	 * @return
	 */
	@GetMapping(value = "/places/{place_id}")
	public Dto<Map<String, Object>>	selectPlaceDetail(@PathVariable String place_id, PlaceDetailDto detail_dto){
		Dto<Map<String, Object>> return_dto = new Dto<>();
		Map<String, String> return_map = new HashMap<>();

		try {
			if(detail_dto.getLatitude() == null || detail_dto.getLongitude() == null || detail_dto.getLatitude().isBlank() || detail_dto.getLongitude().isBlank())
				throw new Exception("latitude & longitude 파라미터를 확인해주세요. 해당 파라미터는 필수입니다.");

			detail_dto.setPlace_id(place_id);
			return_dto.setDataList(placeService.selectPlaceDetail(detail_dto));

		}catch (Exception e) {
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(error);
			return return_dto;
		}


		return return_dto;
	}

	@GetMapping(value = "/places/{place_id}/blogs/naver")
	public Dto<List<Map<String, Object>>> selectNaverBlogs(@PathVariable String place_id,
														   @RequestParam Map<String, String> request_param){
		Dto<List<Map<String, Object>>> return_dto = new Dto<>();

		try{
			request_param.put("place_id", place_id);
			return_dto.setDataList(placeService.selectNaverBlog(request_param));
		}catch (Exception e){
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Arrays.asList(error));
			return return_dto;
		}

		return return_dto;
	}

	@GetMapping(value = "/places/{place_id}/blogs/daum")
	public Dto<List<Map<String, Object>>> selectDaumBlogs(@PathVariable String place_id,
														   @RequestParam Map<String, String> request_param){
		Dto<List<Map<String, Object>>> return_dto = new Dto<>();

		try{
			request_param.put("place_id", place_id);
			return_dto.setDataList(placeService.selectDaumBlog(request_param));
		}catch (Exception e){
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Arrays.asList(error));
			return return_dto;
		}

		return return_dto;
	}

	@GetMapping(value = "/places/{place_id}/reviews/youtube")
	public Dto<List<Map<String, Object>>> selectYoutubeReviews(@PathVariable String place_id,
														  @RequestParam Map<String, String> request_param){
		Dto<List<Map<String, Object>>> return_dto = new Dto<>();

		try{
			request_param.put("place_id", place_id);
			return_dto.setDataList(placeService.selectYoutubeReview(request_param));
		}catch (Exception e){
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Arrays.asList(error));
			return return_dto;
		}

		return return_dto;
	}

	@GetMapping(value = "/places/{place_id}/reviews/google")
	public Dto<List<Map<String, Object>>> selectGoogleReviews(@PathVariable String place_id,
															   @RequestParam Map<String, String> request_param){
		Dto<List<Map<String, Object>>> return_dto = new Dto<>();

		try{
			request_param.put("place_id", place_id);
			return_dto.setDataList(placeService.selectGoogleReview(request_param));
		}catch (Exception e){
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Arrays.asList(error));
			return return_dto;
		}

		return return_dto;
	}

/*
	public Dto<Map<String,  List<PortalReviewDto>>> selectPortalReview(
			@PathVariable String place_id,
			@RequestParam(name = "page", defaultValue = "0") String start_index,
			@RequestParam(name = "portal", defaultValue = "ALL") String portal) {

		Dto<Map<String,  List<PortalReviewDto>>> return_dto = new Dto<Map<String, List<PortalReviewDto>>>();
		PlaceDetailDto place_dto = new PlaceDetailDto();
		Map<String, List<PortalReviewDto>> review_dto = Maps.newHashMap();
		Map<String, String> parameter = Maps.newHashMap();

		try {
			parameter.put("portal", portal.toUpperCase());
			parameter.put("start_index", start_index);

			place_dto.setPlace_id(place_id);
//			place_dto = placeService.selectPlaceDetail(place_dto).getDataList();

			review_dto = placeService.selectPlacePortalReview(place_dto, parameter);
			return_dto.setDataList(review_dto);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return return_dto;
	}
*/

/*
	@GetMapping(value = "/places/{place_id}/portalreviews")
	public Dto<Map<String, Object>> selectPortalReviews(
			@PathVariable String place_id,
			@RequestParam(name = "page", defaultValue = "0") String start_index,
			@RequestParam(name = "portal", defaultValue = "ALL") String portal) {

		Dto<Map<String, Object>> return_dto = new Dto<>();
		Map<String, String> parameter = new HashMap<>();

		try {
			if (place_id == null || place_id.isBlank())
				throw new Exception("필수 파라미터를 확인해주세요.");
			if (!NumberUtils.isCreatable(start_index))
				throw new Exception("page 파라미터를 확인해주세요.");
			if (Integer.parseInt(start_index) < 0)
				throw new Exception("page 파라미터를 확인해주세요.");
			if (!(portal.equalsIgnoreCase("YOUTUBE") || portal.equalsIgnoreCase("ALL")))
				throw new Exception("portal 파라미터를 확인해주세요.");

			parameter.put("place_id", place_id);
			parameter.put("portal", portal.toUpperCase());
			parameter.put("start_index", start_index);

			return_dto.setDataList(placeService.selectPlacePortalReviews(parameter));

		}catch (Exception e){
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(error);
			return return_dto;
		}

		return return_dto;
	}
*/


//	/**
//	 * 블로그 조회(naver, tistory)
//	 * @param place_id
//	 */
//	@GetMapping(value = "/places/{place_id}/portalblogs")
//	public Dto<Map<String,List<PortalBlogDto>>> selectPortalBlog(
//			@PathVariable String place_id,
//			@RequestParam(name = "page", defaultValue = "0") String start_page,
//			@RequestParam(name = "portal", defaultValue = "ALL") String portal) {
//
//		Dto<Map<String,List<PortalBlogDto>>> return_dto = new Dto<Map<String,List<PortalBlogDto>>>();
//		PlaceDetailDto place_dto = new PlaceDetailDto();
//		Map<String, List<PortalBlogDto>> blog_dto = Maps.newHashMap();
//		Map<String, String> parameter = Maps.newHashMap();
//
//		if(place_id == null || place_id.isBlank()) {
//			return_dto.setCode(400);
//			return_dto.setMessage("필수 파라미터를 확인해주세요.");
//			return return_dto;
//		}
//		if (!NumberUtils.isCreatable(start_page)) {
//			return_dto.setCode(400);
//			return_dto.setMessage("page 파라미터를 확인해주세요.");
//			return return_dto;
//		}
//		if (Integer.parseInt(start_page) < 0) {
//			return_dto.setCode(400);
//			return_dto.setMessage("page 파라미터를 확인해주세요.");
//			return return_dto;
//		}
//		if(!(portal.equalsIgnoreCase("NAVER") || portal.equalsIgnoreCase("DAUM") || portal.equalsIgnoreCase("ALL"))) {
//			return_dto.setCode(400);
//			return_dto.setMessage("portal 파라미터를 확인해주세요.");
//			return return_dto;
//		}
//
//		try {
//			parameter.put("portal", portal.toUpperCase());
//			parameter.put("start_page", start_page);
//
//			place_dto.setPlace_id(place_id);
////			place_dto = placeService.selectPlaceDetail(place_dto).getDataList();
//
//			blog_dto = placeService.selectPlacePortalBlog(place_dto);
//			return_dto.setDataList(blog_dto);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return return_dto;
//	}

	/**
	 * 인기 검색어
	 * @return
	 */
	@GetMapping("/popularsearches")
	public Dto<List<Object>> getPopularSearches(){
		Dto<List<Object>> return_dto = new Dto<>();

		try {

			return_dto.setDataList(placeService.getPopularSearches());

		} catch (Exception e) {
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());

			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dto.setDataList(Arrays.asList(error));
			return return_dto;
		}

		return return_dto;
	}
}
