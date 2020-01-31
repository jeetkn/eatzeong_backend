package com.place.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import com.place.dto.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.place.api.google.GoogleCustomSearch;
import com.place.api.google.GoogleFindPlace;
import com.place.api.google.GooglePlaceDetail;
import com.place.api.kakao.KakaoSearchApi;
import com.place.repository.PlaceRepository;
import com.place.repository.PortalBlogRepository;
import com.place.repository.PortalReviewRepository;
import com.place.service.PlaceService;
import com.place.service.interfaces.PlaceServiceInterface;

@Service("com.place.service.PlaceService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
public class PlaceService implements PlaceServiceInterface {

	@Inject
	PlaceRepository place;
	@Inject
	PortalReviewRepository review;
	@Inject
	PortalBlogRepository blog;

	/**
	 * 장소 목록 조회
	 */
	@Override
	public Dto<List<PlaceDto>> selectPlaceList(PlaceDto dto) throws Exception {
		KakaoSearchApi kakao_search_api = new KakaoSearchApi();
		Map<String, String> fields = new HashMap<String, String>();
		Dto<List<PlaceDto>> list = new Dto<List<PlaceDto>>();
		List<PlaceDto> list_dto = new ArrayList<PlaceDto>();

		if (dto.getFilter_category() == null || dto.getFilter_category().isBlank()) {
			dto.setFilter_category("ALL");
		}

		list_dto = place.selectPlaceList(dto);

		if (list_dto.size() <= 0) {
			for (int page = 1; page < 4; page++) { // 3번 반복
				fields.put("query", dto.getKeyword());
				fields.put("size", "15");
				fields.put("page", Integer.toString(page));
				fields.put("category_group_code", "FD6,CE7"); // 음식점, 카페

				String json_str = kakao_search_api.callApi(kakao_search_api.CreateURL(fields));
				System.out.println(json_str);
				insertResultApi(json_str, dto.getKeyword());
			}
		}

		list_dto = place.selectPlaceList(dto);

		list.setDataList(list_dto);
		list.setCode(200);
		list.setMessage("호출 성공");
		return list;
	}

	/**
	 * 장소 디테일 정보 조회
	 */
	@Override
	public Dto<PlaceDetailDto> selectPlaceDetail(PlaceDetailDto dto) throws Exception {
		Dto<PlaceDetailDto> detail_dto = new Dto<PlaceDetailDto>();
		GoogleFindPlace google_find_place = new GoogleFindPlace();
		GooglePlaceDetail place_detail = new GooglePlaceDetail();
		Map<String, String> fields = new HashMap<String, String>();
		String google_find_place_API_json = ""; // Find Place API Call
		String google_place_detail_API_json = ""; // Place Detail API Call

		try {
			dto = place.selectPlaceDetail(dto);
			if (dto.getGoogle_place_id() == null || dto.getGoogle_place_id().isBlank()) {
				// Find Place API Call
				fields.put("input", dto.getPlace_name());
				fields.put("language", "ko");
				fields.put("fields", "photos,formatted_address,name,rating,opening_hours,geometry,place_id");
				fields.put("locationbias", "point:" + dto.getLatitude() + "," + dto.getLongitude());
				fields.put("inputtype", "textquery");

				google_find_place_API_json = google_find_place.callApi(google_find_place.CreateURL(fields));
//				System.out.println(google_find_place_API_json);

				if (!JsonPath.parse(google_find_place_API_json).read("$.status").equals("ZERO_RESULTS")) {
					String google_place_name = JsonPath.parse(google_find_place_API_json).read("$.candidates[0].name");
					String google_place_id = JsonPath.parse(google_find_place_API_json)
							.read("$.candidates[0].place_id");

					// Google Place Detail API Call
					fields.clear();
					fields.put("place_id", google_place_id);
					fields.put("language", "ko");
					fields.put("fields", "name,rating,formatted_phone_number,review,opening_hours");

					google_place_detail_API_json = place_detail.callApi(place_detail.CreateURL(fields));
//					System.out.println(google_place_detail_API_json);
					DocumentContext document = JsonPath.parse(google_place_detail_API_json);

					List<Map<String, Map<String, Integer>>> opening_hours = document.read("$.result[?(@.opening_hours)].opening_hours.periods[*]");
					List<String> weekday_text = document.read("$.result[?(@.opening_hours)].opening_hours.weekday_text[*]");
					List<String> tel_no = document.read("$.result[?(@.formatted_phone_number)].formatted_phone_number");

					if (!opening_hours.isEmpty()) { // response json 데이터 중 periods 데이터가 없을 경우
						String jsonstr = openingHourParse(opening_hours);

						dto.setOpen_hours(jsonstr);
						dto.setBuisness_day(weekday_text.toString());

					} else {
						System.out.println("Opening hours 없음!!!");
					}

					if (!tel_no.isEmpty()) {
						dto.setTel_no(tel_no.get(0));
					}
					dto.setGoogle_place_id(google_place_id);
					dto.setGoogle_place_name(google_place_name);
					place.updatePlace(dto);
					dto = place.selectPlaceDetail(dto);
					detail_dto.setDataList(dto);
				} else {
					System.out.println("Result값 없음");
					detail_dto.setDataList(dto);
				}
			} else {
				detail_dto.setDataList(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return detail_dto;
	}

	/**
	 * 포탈 리뷰 조회(유튜브, 구글)
	 */
	@Override
	public Map<String, List<PortalReviewDto>> selectPlacePortalReview(PlaceDetailDto place_dto, Map<String, String> parameter) {
		Map<String, List<PortalReviewDto>> result_map = Maps.newHashMap();
		List<PortalReviewDto> youtube_dto = Lists.newArrayList();
		List<PortalReviewDto> google_dto = Lists.newArrayList();

		try {
			List<PortalReviewDto> review_list = review.selectReviews(place_dto); // 리뷰 전체 조회
			if (review_list.isEmpty()) {
				youtube_dto = insertYoutubeReview(place_dto); // 유튜브 리뷰 삽입
				google_dto = insertGoogleReview(place_dto); // 구글 리뷰 삽입
			} else {
				int y_cnt = 0; // 유튜브 리뷰 갯수
				int g_cnt = 0; // 구글 리뷰 갯수
				for (PortalReviewDto dto : review_list) {
					if (dto.getPortal().equalsIgnoreCase("Y")) {
						youtube_dto.add(dto);
						y_cnt++;
					} else if (dto.getPortal().equalsIgnoreCase("G")) {
						google_dto.add(dto);
						g_cnt++;
					}
				}
				if (y_cnt < 1) {
					youtube_dto = insertYoutubeReview(place_dto);
				}
				if (g_cnt < 1) {
					google_dto = insertGoogleReview(place_dto);
				}
			}

			result_map.put("YOUTUBE", youtube_dto);
			result_map.put("GOOGLE", google_dto);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result_map;
	}

	/**
	 * 포털 블로그 조회(네이버, 다음)
	 */
	@Override
	public Map<String, List<PortalBlogDto>> selectPlacePortalBlog(PlaceDetailDto place_dto) {
		Map<String, List<PortalBlogDto>> result_map = Maps.newHashMap();
		List<PortalBlogDto> naver_dto = Lists.newArrayList();
		List<PortalBlogDto> daum_dto = Lists.newArrayList();

		

		try {
			List<PortalBlogDto> blog_list = blog.selectBlog(place_dto); // 블로그 전체 조회
			if (blog_list.isEmpty()) {
				naver_dto = insertNaverBlog(place_dto);
				daum_dto = insertDaumBlog(place_dto);
			} else {
				int gn_cnt = 0;
				int gt_cnt = 0;
				for (PortalBlogDto dto : blog_list) {
					if (dto.getPortal().equalsIgnoreCase("GN")) { // 구글 서치 네이버
						naver_dto.add(dto);
						gn_cnt++;
					} else if (dto.getPortal().equalsIgnoreCase("GT")) { // 구글 서치 다음(티스토리)
						daum_dto.add(dto);
						gt_cnt++;
					}
				}
				if (gn_cnt < 1)
					naver_dto = insertNaverBlog(place_dto);
				if (gt_cnt < 1)
					daum_dto = insertDaumBlog(place_dto);
			}

			result_map.put("NAVER", naver_dto);
			result_map.put("DAUM", daum_dto);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result_map;
	}

	/**
	 * <strong>유튜브 리뷰 삽입</strong>
	 * 
	 * @param place_dto
	 * @return
	 * @throws Exception
	 */
	private List<PortalReviewDto> insertYoutubeReview(PlaceDetailDto place_dto) throws Exception {
		List<PortalReviewDto> review_list = Lists.newArrayList();
		PortalReviewDto review_dto = new PortalReviewDto();
		Map<String, String> fields = new HashMap<String, String>();
		GoogleCustomSearch custom_search = new GoogleCustomSearch();
		String youtube_search_API_json = ""; // Youtube Search API Call

		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String last_year_date = LocalDateTime.now().minusYears(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		fields.put("q", place_dto.getPlace_name());
		fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 2년

		try {
			youtube_search_API_json = custom_search.callApi(custom_search.CreateURL(fields), "YOUTUBE");
			review_list = parseYoutube(JsonPath.parse(youtube_search_API_json), place_dto);

			review.insertReview(review_list);

			review_dto.setPlace_id(place_dto.getPlace_id());
			review_dto.setPortal("Y");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return review.selectPortalReview(review_dto);
	}

	/**
	 * <strong>구글 리뷰 삽입</strong>
	 * 
	 * @param place_dto
	 * @return
	 */
	private List<PortalReviewDto> insertGoogleReview(PlaceDetailDto place_dto) {
		List<PortalReviewDto> review_list = Lists.newArrayList();
		List<PortalReviewDto> resultData = Lists.newArrayList();
		
		PortalReviewDto review_dto = new PortalReviewDto();
		Map<String, String> fields = new HashMap<String, String>();
		GooglePlaceDetail place_detail = new GooglePlaceDetail();
		String google_placd_detail_API_json = "";

		if (!(place_dto.getGoogle_place_id() == null || place_dto.getGoogle_place_id().isEmpty())) {
			try {
				fields.put("place_id", place_dto.getGoogle_place_id());
				fields.put("language", "ko");
				fields.put("fields", "name,rating,formatted_phone_number,review,opening_hours");

				google_placd_detail_API_json = place_detail.callApi(place_detail.CreateURL(fields));
				review_list = parseGoogle(JsonPath.parse(google_placd_detail_API_json), place_dto);

				review.insertReview(review_list);

				review_dto.setPlace_id(place_dto.getPlace_id());
				review_dto.setPortal("G");
				
				resultData = review.selectPortalReview(review_dto);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return resultData;
	}

	/**
	 * <strong>네이버 블로그 삽입</strong>
	 * 
	 * @param place_dto
	 * @return
	 */
	private List<PortalBlogDto> insertNaverBlog(PlaceDetailDto place_dto) {
		String json = "";
		GoogleCustomSearch custom_search = new GoogleCustomSearch();
		Map<String, String> fields = new HashMap<String, String>();
		PortalBlogDto blog_dto = new PortalBlogDto();
		List<PortalBlogDto> blog_list = Lists.newArrayList();

		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String last_year_date = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		fields.put("q", place_dto.getPlace_name());
		fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 6개월

		try {

			json = custom_search.callApi(custom_search.CreateURL(fields), "NAVER");
			blog_list = parseNaver(JsonPath.parse(json), place_dto);

			blog.insertBlog(blog_list);

			blog_dto.setPlace_id(place_dto.getPlace_id());
			blog_dto.setPortal("GN");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blog.selectPortalBlog(blog_dto);
	}

	/**
	 * <strong>다음 블로그 삽입</strong>
	 * 
	 * @param place_dto
	 * @return
	 */
	private List<PortalBlogDto> insertDaumBlog(PlaceDetailDto place_dto) {
		String json = "";
		GoogleCustomSearch custom_search = new GoogleCustomSearch();
		Map<String, String> fields = new HashMap<String, String>();
		PortalBlogDto blog_dto = new PortalBlogDto();

		try {
			
			List<PortalBlogDto> blog_list = blog.selectBlog(place_dto);

			String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String last_year_date = LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			fields.put("q", place_dto.getPlace_name());
			fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 1년
			

			json = custom_search.callApi(custom_search.CreateURL(fields), "DAUM");
			blog_list = parseDaum(JsonPath.parse(json), place_dto);

			blog.insertBlog(blog_list);

			blog_dto.setPlace_id(place_dto.getPlace_id());
			blog_dto.setPortal("GT");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blog.selectPortalBlog(blog_dto);
	}

	/**
	 * 유튜브 리뷰 데이터 dto setting
	 * 
	 * @param document
	 * @param place_dto
	 * @return
	 */
	private List<PortalReviewDto> parseYoutube(DocumentContext document, PlaceDetailDto place_dto) throws Exception {
		List<PortalReviewDto> review_list = Lists.newArrayList();
		ObjectMapper oMapper = new ObjectMapper();
		Map<String, String> request = (Map<String, String>) document.read("$.YOUTUBE.queries.request[0]");

		if (Integer.parseInt(request.get("totalResults")) > 0) {
			List<Map<String, String>> items = document.read("$.YOUTUBE.items[*]");
			items.stream()
					.filter(item -> {
						Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
						return map.containsKey("videoobject")
								&& map.get("videoobject").get(0).get("genre").equalsIgnoreCase("People & Blogs");
					})
					.forEach(item -> {
						Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
						PortalReviewDto dto = new PortalReviewDto();

						dto.setPlace_id(place_dto.getPlace_id());
						dto.setPortal("Y");
						dto.setAuthor(map.get("videoobject").get(0).get("channelid"));
						dto.setY_video_id(map.get("metatags").get(0).get("og:url"));
						dto.setY_title(map.get("metatags").get(0).get("title"));
						dto.setY_description(map.get("metatags").get(0).get("og:description"));
						dto.setY_thumbnail_url(map.get("videoobject").get(0).get("thumbnailurl"));
						dto.setWrite_date(map.get("videoobject").get(0).get("uploaddate"));
						dto.setWrite_time("00:00:00");
						dto.setY_start_index(String.valueOf(request.get("startIndex")));

						review_list.add(dto);
					});
		} else {
			throw new Exception("검색 결과가 없습니다.");
		}

		return review_list;
	}

	/**
	 * 구글 리뷰 데이터 dto setting
	 * 
	 * @param document
	 * @param place_dto
	 * @return
	 */
	private List<PortalReviewDto> parseGoogle(DocumentContext document, PlaceDetailDto place_dto) {
		List<PortalReviewDto> review_list = Lists.newArrayList();

		List<Map<String, Object>> google_reviews = document.read("$.result.reviews");
		if (!google_reviews.isEmpty()) {
			for (Map<String, Object> review_line : google_reviews) {
				PortalReviewDto review_dto = new PortalReviewDto();
				review_dto.setAuthor(review_line.get("author_name").toString());
				review_dto.setG_rating(Integer.parseInt(review_line.get("rating").toString()));
				review_dto.setG_content(review_line.get("text").toString().replaceAll("(\r\n|\r|\n|\n\r)", ""));
				review_dto.setWrite_date(unixTimeToDateTime(review_line.get("time").toString(), "date"));
				review_dto.setWrite_time(unixTimeToDateTime(review_line.get("time").toString(), "time"));
				review_dto.setPlace_id(place_dto.getPlace_id());
				review_dto.setPortal("G");

				review_list.add(review_dto);
			}
		}

		return review_list;
	}

	/**
	 * 네이버 블로그 dto setting
	 * 
	 * @param document
	 * @param place_dto
	 * @return
	 */
	private List<PortalBlogDto> parseNaver(DocumentContext document, PlaceDetailDto place_dto) {
		List<PortalBlogDto> dto_list = new ArrayList<PortalBlogDto>();
		Map<String, String> request = document.read("$.NAVER.queries.request[0]");

		if (Integer.parseInt(request.get("totalResults")) > 0) {
			List<Map<String, String>> item_list = document.read("$.NAVER.items[*].pagemap.metatags[*]");
			List<String> write_date_list = document.read("$.NAVER.items[*].snippet");
			int index = 0;
			for (Map<String, String> item : item_list) {
				String write_date_origin = write_date_list.get(index++);
				String write_date = write_date_origin.substring(0, write_date_origin.indexOf("일") + 1);

				try {
					SimpleDateFormat origin_format = new SimpleDateFormat("yyyy년 M월 d일");
					SimpleDateFormat new_format = new SimpleDateFormat("yyyy-MM-dd");
					write_date = LocalDate.parse(new_format.format(origin_format.parse(write_date)))
							.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

					if (item.get("og:title") != null && item.get("naverblog:nickname") != null) {
						PortalBlogDto naver_dto = new PortalBlogDto();
						naver_dto.setPortal("GN");
						naver_dto.setPlace_id(place_dto.getPlace_id());
						naver_dto.setTitle(item.get("og:title"));
						naver_dto.setAuthor(item.get("naverblog:nickname"));
						naver_dto.setUrl(item.get("og:url"));
						naver_dto.setDescription(item.get("og:description"));
						naver_dto.setWrite_date(write_date);
						naver_dto.setWrite_time("00:00:00");
						naver_dto.setThumbnail_url(item.get("og:image"));

						dto_list.add(naver_dto);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		return dto_list;
	}

	/**
	 * 다음 블로그 dto setting
	 * 
	 * @param document
	 * @param place_dto
	 * @return
	 */
	private List<PortalBlogDto> parseDaum(DocumentContext document, PlaceDetailDto place_dto) {
		List<PortalBlogDto> dto_list = new ArrayList<PortalBlogDto>();
		Map<String, String> request = document.read("$.DAUM.queries.request[0]");

		if (Integer.parseInt(request.get("totalResults")) > 0) {
			List<Map<String, String>> item_list = document.read("$.DAUM.items[*].pagemap.metatags[*]");
			for (Map<String, String> item : item_list) {
				if (item.get("title") != null && item.get("by") != null) {
					PortalBlogDto daum_dto = new PortalBlogDto();
					daum_dto.setPortal("GT");
					daum_dto.setPlace_id(place_dto.getPlace_id());
					daum_dto.setTitle(item.get("title"));
					daum_dto.setAuthor(item.get("by"));
					daum_dto.setUrl(item.get("og:url"));
					daum_dto.setDescription(item.get("og:description"));
					daum_dto.setWrite_date(ZonedDateTime.parse(item.get("article:published_time"))
							.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
					daum_dto.setWrite_time(ZonedDateTime.parse(item.get("article:published_time"))
							.format(DateTimeFormatter.ofPattern("hh:mm:ss")));
					daum_dto.setThumbnail_url(item.get("og:image"));

					dto_list.add(daum_dto);
				}
			}
		}
		return dto_list;
	}

	/**
	 * 장소 정보 Insert
	 * 
	 * @param json_str
	 * @param keyword
	 */
	private void insertResultApi(String json_str, String keyword) throws Exception{
		List<PlaceDto> dto_list = new ArrayList<PlaceDto>();
		DocumentContext document = JsonPath.parse(json_str);
		List<Map<String, String>> place_list = document.read("$.documents");
		for (Map<String, String> map : place_list) {
			PlaceDto dto = new PlaceDto();
			String category_division[] = (map.get("category_name")).split(" > ");
			switch (category_division[1]) {
			case "카페":
				dto.setCategory("CE7");
				break;
			case "한식":
				dto.setCategory("CT1");
				break;
			case "중식":
				dto.setCategory("CT2");
				break;
			case "양식":
				dto.setCategory("CT3");
				break;
			case "일식":
				dto.setCategory("CT4");
				break;
			case "뷔페":
				dto.setCategory("CT5");
				break;
			case "술집":
				dto.setCategory("CT6");
				break;
			case "분식":
				dto.setCategory("CT7");
				break;
			default:
				dto.setCategory("CT8");
				break;
			}
			dto.setPlace_address(map.get("address_name"));
			dto.setRoad_place_address(map.get("road_address_name"));
			dto.setTel_no(map.get("phone"));
			dto.setPlace_name(map.get("place_name"));
			dto.setLatitude(map.get("y"));
			dto.setLongitude(map.get("x"));
			dto.setPlace_id(map.get("id") + "K");
			dto.setKakao_place_id(map.get("id"));
			dto.setCategory_detail(map.get("category_name"));
			dto.setParent_category("FD6");
			dto.setCategory_name(category_division[1]);
			dto.setKeyword(keyword);

			dto_list.add(dto);
		}
		place.insertPlace(dto_list);
	}

	/**
	 * 영업시간 데이터 파싱
	 * 
	 * @param opening_hours_list
	 * @return
	 */
	private String openingHourParse(List<Map<String, Map<String, Integer>>> opening_hours_list) {
		Table<String, String, String> opening_hours_table = HashBasedTable.create();
		String[] days = new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fir", "Sat" };
		String jsonString = null;
		try {
			for (int i = 0; i < 7; i++) {
				List<String> open_time = new ArrayList<String>();
				List<String> close_time = new ArrayList<String>();
				String close_nextday = "";
				for (Map<String, Map<String, Integer>> opening_hours_map : opening_hours_list) {
					if (opening_hours_map.get("open").get("day") == i) {
						if (opening_hours_map.containsKey("open") && opening_hours_map.containsKey("close")) {
							if (opening_hours_map.get("open").get("day") != opening_hours_map.get("close").get("day")) {
								close_nextday = "Y";
								opening_hours_table.put(days[i], "close_nextday", close_nextday);
							}else {
								close_nextday = "N";
								opening_hours_table.put(days[i], "close_nextday", close_nextday);
							}
//							System.out.println(opening_hours_map.toString());
							open_time.add(String.valueOf(opening_hours_map.get("open").get("time")));
							close_time.add(String.valueOf(opening_hours_map.get("close").get("time")));
						} else {
							open_time.add("0"); // 24시간 영업
							close_time.add("0000");
						}
					}
				}
//				System.out.println(days[i] + " open : " + open_time + " close : " + close_time + " close_nextday : " + close_nextday);
				if (!open_time.isEmpty())
					opening_hours_table.put(days[i], "open", Collections.min(open_time));
				else
					opening_hours_table.put(days[i], "open", "");
				if (!close_time.isEmpty())
					opening_hours_table.put(days[i], "close",
							(close_nextday.equals("Y")) ? Collections.min(close_time) : Collections.max(close_time));
				else
					opening_hours_table.put(days[i], "close", "");
				opening_hours_table.put(days[i], "close_nextday", close_nextday);
//				System.out.println("table_" + days[i] + " : " + opening_hours_table.row(days[i]));
			}

			ObjectMapper objMapper = new ObjectMapper().registerModule(new GuavaModule());
			jsonString = objMapper.writeValueAsString(opening_hours_table);
//			System.out.println(jsonString);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	/**
	 * 유닉스 타임값 date/time으로 변환
	 * 
	 * @param unix   유닉스 타임값
	 * @param format 반환 형식(date/time)
	 * @return String date(YYYY-MM-DD)/time(HH:MM:SS)
	 */
	public String unixTimeToDateTime(String unix, String format) {
		long t = Long.parseLong(unix + "000");
		SimpleDateFormat simpleDate = new SimpleDateFormat();

		if (format.equals("date")) {
			simpleDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		} else {
			simpleDate = new SimpleDateFormat("hh:mm:ss", Locale.KOREA);
		}

		return simpleDate.format(t);
	}

}
