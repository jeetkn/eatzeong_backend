package com.place.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.place.api.google.GoogleCustomSearch;
import com.place.api.google.GoogleFindPlace;
import com.place.api.google.GooglePlaceDetail;
import com.place.api.kakao.KakaoSearchApi;
import com.place.dto.*;
import com.place.exception.ExistException;
import com.place.exception.ThrowingFunction;
import com.place.repository.PlaceRepository;
import com.place.repository.PortalBlogRepository;
import com.place.repository.PortalReviewRepository;
import com.place.service.interfaces.PlaceServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service("com.place.service.PlaceService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
@Slf4j
public class PlaceService implements PlaceServiceInterface {

	@Inject
	PlaceRepository place_repo;
	@Inject
	PortalReviewRepository review_repo;
	@Inject
	PortalBlogRepository blog_repo;
	@Inject
	GoogleCustomSearch custom_search;
	@Autowired
	ApiService apiService;
	@Value("${host.url}")
	private String HOST_URL;

	/**
	 * 장소 목록 조회
	 */
	@Override
	public List<Object> selectPlaceList(PlaceDto dto) throws Exception{
		KakaoSearchApi kakao_search_api = new KakaoSearchApi();
		Map<String, String> fields = new HashMap<String, String>();
		Dto<List<PlaceDto>> list = new Dto<List<PlaceDto>>();
		List<PlaceDto> list_dto = new ArrayList<PlaceDto>();
		List<Object> return_list = new ArrayList<>();

		if(dto.getUser_id() == null || "".equals(dto.getUser_id())) {
			dto.setUser_id("temp");
			dto.setSns_division("T");
		}

		log.info(dto.toString());
		place_repo.insertKeyword(dto);

		if (dto.getFilter_category() == null || dto.getFilter_category().isBlank()) {
			dto.setFilter_category("ALL");
		}

		list_dto = place_repo.selectPlaceList(dto);

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

		list_dto = place_repo.selectPlaceList(dto);

		list_dto.stream()
				.forEach(res -> {
					Map<String, Object> temp_map = new LinkedHashMap<>();
					temp_map.put("place_name", res.getPlace_name());
					temp_map.put("place_id", res.getPlace_id());
					temp_map.put("latitude", res.getLatitude());
					temp_map.put("longitude", res.getLongitude());
					temp_map.put("place_address", res.getPlace_address());
					temp_map.put("road_place_address", res.getRoad_place_address());
					temp_map.put("open_hours", res.getOpen_hours());
					temp_map.put("google_rating", res.getGoogle_rating());
					temp_map.put("app_rating", res.getApp_rating());
					temp_map.put("google_place_id", res.getGoogle_place_id());
					temp_map.put("kakao_place_id", res.getKakao_place_id());
					temp_map.put("naver_place_id", res.getNaver_place_id());
					if(res.getBlog_thumbnail() == null || res.getBlog_thumbnail().isBlank())
						temp_map.put("blog_thumbnail", null);
					else
						temp_map.put("blog_thumbnail", res.getBlog_thumbnail());
					temp_map.put("app_thumbnail", res.getApp_thumbnail());
					temp_map.put("category_name", res.getCategory_name());
					temp_map.put("naver_blog_count", res.getNaver_blog_count());
					temp_map.put("daum_blog_count", res.getDaum_blog_count());
					temp_map.put("google_review_count", res.getGoogle_review_count());
					temp_map.put("youtube_review_count", res.getYoutube_review_count());
					temp_map.put("app_review_count", res.getApp_review_count());

					return_list.add(temp_map);
				});

		return return_list;
	}

	/**
	 * 장소 상세 조회
	 *
	 * @param detail_dto
	 * @return
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> selectPlaceDetail(PlaceDetailDto detail_dto) throws Exception{
		Map<String, Object> return_map = new LinkedHashMap<>();
		Map<String, String> fields = new HashMap<String, String>();
		GoogleFindPlace google_find_place = new GoogleFindPlace();
		GooglePlaceDetail place_detail = new GooglePlaceDetail();
		String google_find_place_API_json = ""; // Find Place API Call
		String google_place_detail_API_json = ""; // Place Detail API Call
		String user_id = detail_dto.getUser_id();
		String sns_division = detail_dto.getSns_division();

		detail_dto = place_repo.selectPlaceDetail(detail_dto);

		if (detail_dto.getGoogle_place_id() == null || detail_dto.getGoogle_place_id().isBlank()) {
			// Find Place API Call
			fields.put("input", detail_dto.getPlace_name());
			fields.put("language", "ko");
			fields.put("fields", "photos,formatted_address,name,rating,opening_hours,geometry,place_id");
			fields.put("locationbias", "point:" + detail_dto.getLatitude() + "," + detail_dto.getLongitude());
			fields.put("inputtype", "textquery");

			google_find_place_API_json = google_find_place.callApi(google_find_place.CreateURL(fields));

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
				DocumentContext document = JsonPath.parse(google_place_detail_API_json);

				List<Map<String, Map<String, Integer>>> opening_hours = document.read("$.result[?(@.opening_hours)].opening_hours.periods[*]");
				List<String> weekday_text = document.read("$.result[?(@.opening_hours)].opening_hours.weekday_text[*]");
				List<String> tel_no = document.read("$.result[?(@.formatted_phone_number)].formatted_phone_number");

				if (!opening_hours.isEmpty()) { // response json 데이터 중 periods 데이터가 없을 경우
					String jsonstr = openingHourParse(opening_hours);

					detail_dto.setOpen_hours(jsonstr);
					detail_dto.setBuisness_day(weekday_text.toString());

				} else {
					System.out.println("Opening hours 없음!!!");
				}

				if (!tel_no.isEmpty()) {
					detail_dto.setTel_no(tel_no.get(0));
				}
				detail_dto.setGoogle_place_id(google_place_id);
				detail_dto.setGoogle_place_name(google_place_name);
				place_repo.updatePlace(detail_dto);
				detail_dto = place_repo.selectPlaceDetail(detail_dto);

			} else {
				log.info("Result값 없음");
			}
		}

		detail_dto.setUser_id(user_id);
		detail_dto.setSns_division(sns_division);
		int bookmark_flag = place_repo.selectPlaceBookmark(detail_dto);
		int appreview_flag = place_repo.selectPlaceAppReviewFlag(detail_dto);

		return_map.put("place_name", detail_dto.getPlace_name());
		return_map.put("place_id", detail_dto.getPlace_id());
		return_map.put("google_place_id", detail_dto.getGoogle_place_id());
		return_map.put("phone_no", detail_dto.getTel_no());
		return_map.put("opening_hours", detail_dto.getOpen_hours());
		if(detail_dto.getBuisness_day() != null)
			return_map.put("business_day", detail_dto.getBuisness_day().replaceAll(": ", "  "));
		else
			return_map.put("business_day", null);
		return_map.put("road_address", detail_dto.getRoad_place_address());
		return_map.put("address", detail_dto.getPlace_address());
		return_map.put("google_rating", detail_dto.getGoogle_rating());
		return_map.put("app_rating", detail_dto.getApp_rating());
		return_map.put("category", detail_dto.getCategory_name());
		return_map.put("bookmark_flag", (bookmark_flag > 0) ? true : false);
		return_map.put("appreview_flag", (appreview_flag > 0) ? true : false);

		return return_map;
	}

	/**
	 * 포탈 리뷰 조회(유튜브, 구글)
	 */
/*	@Override
	public Map<String, List<PortalReviewDto>> selectPlacePortalReview(PlaceDetailDto place_dto, Map<String, String> parameter) {
		Map<String, List<PortalReviewDto>> result_map = Maps.newHashMap();
		List<PortalReviewDto> youtube_dto = Lists.newArrayList();
		List<PortalReviewDto> google_dto = Lists.newArrayList();

		try {
			Map<String, Object> detail_dto_map = selectPlaceDetail(place_dto);
			place_dto.setPlace_name(detail_dto_map.get("place_name").toString());
			List<PortalReviewDto> review_list = review_repo.selectReviews(place_dto); // 리뷰 전체 조회
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
	}*/

	/**
	 * 포털 블로그 조회(네이버, 다음)
	 */
/*	@Override
	public Map<String, List<PortalBlogDto>> selectPlacePortalBlog(PlaceDetailDto place_dto) {
		Map<String, List<PortalBlogDto>> result_map = Maps.newHashMap();
		List<PortalBlogDto> naver_dto = Lists.newArrayList();
		List<PortalBlogDto> daum_dto = Lists.newArrayList();



		try {
			List<PortalBlogDto> blog_list = blog_repo.selectBlog(place_dto); // 블로그 전체 조회
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
	}*/

	/**
	 * <strong>유튜브 리뷰 삽입</strong>
	 *
	 * @param place_dto
	 * @return
	 * @throws Exception
	 */
/*	private List<PortalReviewDto> insertYoutubeReview(PlaceDetailDto place_dto) throws Exception {
		List<PortalReviewDto> review_list = Lists.newArrayList();
		PortalReviewDto review_dto = new PortalReviewDto();
		Map<String, String> fields = new HashMap<String, String>();
		GoogleCustomSearch custom_search = new GoogleCustomSearch();
		String youtube_search_API_json = ""; // Youtube Search API Call

		String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String last_year_date = LocalDateTime.now().minusYears(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		fields.put("q", place_dto.getPlace_name());
		fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 2년

		youtube_search_API_json = custom_search.callApi(custom_search.CreateURL(fields), "YOUTUBE");
		review_list = parseYoutube(JsonPath.parse(youtube_search_API_json), place_dto);

		review_repo.insertReview(review_list);

		review_dto.setPlace_id(place_dto.getPlace_id());
		review_dto.setPortal("Y");

		return review_repo.selectPortalReview(review_dto);
	}*/

	/**
	 * <strong>구글 리뷰 삽입</strong>
	 *
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalReviewDto> insertGoogleReview(PlaceDetailDto place_dto) throws Exception {
		List<PortalReviewDto> review_list = Lists.newArrayList();
		List<PortalReviewDto> resultData = Lists.newArrayList();

		PortalReviewDto review_dto = new PortalReviewDto();
		Map<String, String> fields = new HashMap<String, String>();
		GooglePlaceDetail place_detail = new GooglePlaceDetail();
		String google_placd_detail_API_json = "";

		if (!(place_dto.getGoogle_place_id() == null || place_dto.getGoogle_place_id().isEmpty())) {
			fields.put("place_id", place_dto.getGoogle_place_id());
			fields.put("language", "ko");
			fields.put("fields", "name,rating,formatted_phone_number,review,opening_hours");

			google_placd_detail_API_json = place_detail.callApi(place_detail.CreateURL(fields));
			review_list = parseGoogle(JsonPath.parse(google_placd_detail_API_json), place_dto);

			review_repo.insertReview(review_list);

			review_dto.setPlace_id(place_dto.getPlace_id());
			review_dto.setPortal("G");

			resultData = review_repo.selectPortalReview(review_dto);
		}

		return resultData;
	}*/

	/**
	 * <strong>네이버 블로그 삽입</strong>
	 *
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalBlogDto> insertNaverBlog(PlaceDetailDto place_dto) throws Exception {
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

			blog_repo.insertBlog(blog_list);

			blog_dto.setPlace_id(place_dto.getPlace_id());
			blog_dto.setPortal("GN");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blog_repo.selectPortalBlog(blog_dto);
	}*/

	/**
	 * <strong>다음 블로그 삽입</strong>
	 *
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalBlogDto> insertDaumBlog(PlaceDetailDto place_dto) throws Exception {
		String json = "";
		GoogleCustomSearch custom_search = new GoogleCustomSearch();
		Map<String, String> fields = new HashMap<String, String>();
		PortalBlogDto blog_dto = new PortalBlogDto();

		try {

			List<PortalBlogDto> blog_list = blog_repo.selectBlog(place_dto);

			String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			String last_year_date = LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

			fields.put("q", place_dto.getPlace_name());
			fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 1년


			json = custom_search.callApi(custom_search.CreateURL(fields), "DAUM");
			blog_list = parseDaum(JsonPath.parse(json), place_dto);

			blog_repo.insertBlog(blog_list);

			blog_dto.setPlace_id(place_dto.getPlace_id());
			blog_dto.setPortal("GT");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blog_repo.selectPortalBlog(blog_dto);
	}*/

	/**
	 * 유튜브 리뷰 데이터 dto setting
	 *
	 * @param document
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalReviewDto> parseYoutube(DocumentContext document, PlaceDetailDto place_dto) throws Exception {
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
	}*/

	/**
	 * 구글 리뷰 데이터 dto setting
	 *
	 * @param document
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalReviewDto> parseGoogle(DocumentContext document, PlaceDetailDto place_dto) {
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
	}*/

	/**
	 * 네이버 블로그 dto setting
	 *
	 * @param document
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalBlogDto> parseNaver(DocumentContext document, PlaceDetailDto place_dto) {
		List<PortalBlogDto> dto_list = new ArrayList<>();
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
	}*/

	/**
	 * 다음 블로그 dto setting
	 *
	 * @param document
	 * @param place_dto
	 * @return
	 */
/*	private List<PortalBlogDto> parseDaum(DocumentContext document, PlaceDetailDto place_dto) {
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
	}*/

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
		place_repo.insertPlace(dto_list);
	}

	/**
	 * 영업시간 데이터 파싱
	 *
	 * @param opening_hours_list
	 * @return
	 */
	private String openingHourParse(List<Map<String, Map<String, Integer>>> opening_hours_list) {
		Table<String, String, String> opening_hours_table = HashBasedTable.create();
		String[] days = new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
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



/*	public String unixTimeToDateTime(String unix, String format) {
		long t = Long.parseLong(unix + "000");
		SimpleDateFormat simpleDate = new SimpleDateFormat();

		if (format.equals("date")) {
			simpleDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		} else {
			simpleDate = new SimpleDateFormat("hh:mm:ss", Locale.KOREA);
		}

		return simpleDate.format(t);
	}*/

/*	@Override
	public Map<String, Object> selectPlacePortalReviews(Map<String, String> parameter) throws Exception{
		Map<String, Object> return_map = new HashMap<>();
		Map<String, Object> place_detail_map = new HashMap<>();
		List<Map<String, Object>> youtube_list = new ArrayList<>();
		List<Map<String, Object>> google_list = new ArrayList<>();
		List<PortalReviewDto> youtube_dto = new ArrayList<>();
		List<PortalReviewDto> google_dto = new ArrayList<>();

		PlaceDetailDto place_dto = new PlaceDetailDto();
		place_dto.setPlace_id(parameter.get("place_id"));
		place_detail_map = selectPlaceDetail(place_dto);
		place_dto.setPlace_name(place_detail_map.get("place_name").toString());
		place_dto.setGoogle_place_id(place_detail_map.get("google_place_id").toString());
		log.info(place_detail_map.toString());

		List<PortalReviewDto> review_list = review_repo.selectReviews(place_dto); // 리뷰 전체 조회
		if(review_list.isEmpty()){
			insertYoutubeReview(place_dto); // 유튜브 리뷰 삽입
			insertGoogleReview(place_dto); // 구글 리뷰 삽입
		}else{
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
				insertYoutubeReview(place_dto);
			}
			if (g_cnt < 1) {
				insertGoogleReview(place_dto);
			}
		}


		return_map.put("YOUTUBE", youtube_list);
		return_map.put("GOOGLE", google_list);

		return return_map;
	}*/

	@Override
	public List<Object> getPopularSearches() throws Exception{
		List<Object> result_list = new ArrayList<>();
		result_list = place_repo.getPopularSearches().stream()
				.sorted(Comparator.comparing(c -> Integer.parseInt(c.get("index").toString())))
				.collect(Collectors.toList());

		log.info(result_list.toString());

		return result_list;
	}

	@Override
	public List<Map<String, Object>> selectNaverBlog(Map<String, String> request_param) throws Exception{
		Map<String, String> fields_map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		PlaceDetailDto place_detail_dto = new PlaceDetailDto();
		place_detail_dto.setPlace_id(request_param.get("place_id"));
		Map<String, String> place_detail_map = mapper.convertValue(selectPlaceDetail(place_detail_dto), Map.class);
		log.info(place_detail_map.toString());

		fields_map.put("query", request_param.get("query"));
		fields_map.put("place_id", request_param.get("place_id"));
		fields_map.put("place_name", place_detail_map.get("place_name"));
		fields_map.put("user_id", request_param.getOrDefault("user_id", "temp"));
		fields_map.put("sns_division", request_param.getOrDefault("sns_division", "T"));
		fields_map.put("size", request_param.getOrDefault("size", ""));

		int count = place_repo.selectNaverBlogsCount(fields_map);
		if(count < 1) {
			String naver_blog_string = apiService.naverSearchBlog(fields_map);

			List<Map<String, String>> naver_blog_list = JsonPath.parse(naver_blog_string).read("$.items");
			naver_blog_list = naver_blog_list.stream()
					.filter(map -> map.get("link").contains("naver"))
					.peek(map -> {
						map.replace("title", map.get("title").replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""));
						map.replace("title", map.get("title").replaceAll("&#39;", "'"));
						map.replace("description", map.get("description").replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""));
						map.replace("description", map.get("description").replaceAll("&#39;", "'"));
						map.replace("description", map.get("description").replaceAll("&lt;", "<"));
						map.replace("description", map.get("description").replaceAll("&gt;", ">"));
						map.replace("description", map.get("description").replaceAll("&quot;", "\""));
						map.replace("description", map.get("description").replaceAll("&amp;", "&"));

//						try {
//							getOpenGraph(map.get("link"));
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
						log.info(map.toString());
					})
					.collect(Collectors.toList());

			place_repo.insertNaverBlogs(naver_blog_list, fields_map);
		}

		List<Map<String, Object>> result_list = place_repo.selectNaverBlogs(fields_map);
		List<Map<String, Object>> return_list = new ArrayList<>();
		for(Map<String, Object> result_map : result_list){
			Map<String, Object> temp_map = new LinkedHashMap<>();
			temp_map.put("index", result_map.get("row_number"));
			temp_map.put("review_id", result_map.get("review_id"));
			temp_map.put("title", result_map.get("title"));
			temp_map.put("write_date", result_map.get("write_date"));
			temp_map.put("author", result_map.get("author"));
			temp_map.put("description", result_map.get("description"));
			temp_map.put("url", result_map.get("url"));
			temp_map.put("thumbnail_url", result_map.get("thumbnail_url"));
			if(Integer.parseInt(result_map.get("bookmark_flag").toString()) > 0)
				temp_map.put("bookmark_flag", true);
			else
				temp_map.put("bookmark_flag", false);

			return_list.add(temp_map);
		}

		return return_list;
	}

	/*	private List<Map<String, Object>> parseItem(DocumentContext document, Map<String, String> request_param) throws Exception{
		List<Map<String, Object>> result_list = new ArrayList<>();
		org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();

		if(request_param.get("portal").equalsIgnoreCase("NAVER")) {
			SimpleDateFormat origin_format = new SimpleDateFormat("yyyy년 M월 d일");
			SimpleDateFormat new_format = new SimpleDateFormat("yyyy-MM-dd");

			Map<String, String> request = document.read("$.NAVER.queries.request[0]");
			List<Map<String, String>> nextPage = document.read("$.NAVER.queries[?(@.nextPage)].nextPage[0]");

			if (Integer.parseInt(request.get("totalResults")) > 0) {
				List<Map<String, Object>> items = document.read("$.NAVER.items[*]");
				items.stream()
						.forEach(item -> {
							Map<String, List<Map<String, String>>> map = mapper.convertValue(item.get("pagemap"), Map.class);
							Map<String, Object> temp_map = new HashMap<>();
							MainDto dto = new MainDto();
							temp_map.put("place_id", request_param.get("place_id"));
							temp_map.put("portal", "GN");
							temp_map.put("author", map.get("metatags").get(0).get("naverblog:nickname"));
							String[] author_id = map.get("metatags").get(0).get("og:url").split("/");
							temp_map.put("author_id", author_id[3]);
							temp_map.put("write_time", "00:00:00");
							temp_map.put("title", map.get("metatags").get(0).get("og:title"));
							temp_map.put("description", map.get("metatags").get(0).get("og:description").trim());
							temp_map.put("url", map.get("metatags").get(0).get("og:url"));
							temp_map.put("thumbnail_url", map.get("cse_image").get(0).get("src"));
							temp_map.put("start_index", String.valueOf(request.get("startIndex")));
							if(nextPage.size()>0)
								temp_map.put("next_index", String.valueOf(nextPage.get(0).get("startIndex")));
							else
								temp_map.put("next_index", null);

							if(item.get("snippet").toString().indexOf("일")+1 > 10){
								String published_date = item.get("snippet").toString().substring(0, item.get("snippet").toString().indexOf("일") + 1);
								try {
									published_date = LocalDate.parse(new_format.format(origin_format.parse(published_date))).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
									temp_map.put("write_date", published_date);
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}else{
								temp_map.put("write_date", "1900-01-01");
							}

							log.info(temp_map.toString());
							result_list.add(temp_map);
						});
			} else {
				throw new Exception("검색 결과가 없습니다.");
			}
		}else if(request_param.get("portal").equalsIgnoreCase("DAUM")){
			Map<String, String> request = (Map<String, String>) document.read("$.DAUM.queries.request[0]");

			if (Integer.parseInt(request.get("totalResults")) > 0) {
				List<Map<String, Object>> items = document.read("$.DAUM.items[*]");
				items.stream()
						.filter( item -> {
							Map<String, List<Map<String, String>>> map = mapper.convertValue(item.get("pagemap"), Map.class);
							return map.get("metatags").get(0).containsKey("title")
									&& map.get("metatags").get(0).containsKey("by")
									&& map.get("metatags").get(0).containsKey("article:section")
									&& (map.get("metatags").get(0).get("article:section").equals("일상다반사")
									|| map.get("metatags").get(0).get("article:section").equals("맛집")
									|| map.get("metatags").get(0).get("article:section").equals("카페·디저트"))
									&& !map.get("cse_image").get(0).get("src").equals("https://t1.daumcdn.net/tistory_admin/static/images/openGraph/opengraph.png")
									&& map.get("metatags").get(0).get("og:url").contains("tistory.com");
						})
						.forEach(item -> {
							Map<String, List<Map<String, String>>> map = mapper.convertValue(item.get("pagemap"), Map.class);
							MainDto dto = new MainDto();
							dto.setKeyword(request_param.get("query"));
							dto.setPortal(request_param.get("portal").toUpperCase());
							if(map.get("metatags").get(0).get("by").indexOf("사용자 ") >= 0){
								dto.setAuthor(map.get("metatags").get(0).get("by").replace("사용자 ", ""));
							}else{
								dto.setAuthor(map.get("metatags").get(0).get("by"));
							}
							dto.setPublished_date(ZonedDateTime.parse(map.get("metatags").get(0).get("article:published_time"))
									.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
							dto.setThumbnail(map.get("cse_image").get(0).get("src"));
							dto.setTitle(map.get("metatags").get(0).get("og:title"));
							dto.setDescription(map.get("metatags").get(0).get("og:description"));
							dto.setLink(map.get("metatags").get(0).get("og:url"));
							dto.setStart_index(String.valueOf(request.get("startIndex")));
							log.info(dto.toString());
//							dto_list.add(dto);
						});
			} else {
				throw new Exception("검색 결과가 없습니다.");
			}
		}else {
			Map<String, String> request = (Map<String, String>) document.read("$.YOUTUBE.queries.request[0]");

			if (Integer.parseInt(request.get("totalResults")) > 0) {
				List<Map<String, String>> items = document.read("$.YOUTUBE.items[*]");
				items.stream()
						.filter(item -> {
							Map<String, List<Map<String, String>>> map = mapper.convertValue(item.get("pagemap"), Map.class);
							return map.containsKey("videoobject")
									&& map.get("videoobject").get(0).get("genre").equalsIgnoreCase("People & Blogs");
						})
						.forEach(item -> {
							Map<String, List<Map<String, String>>> map = mapper.convertValue(item.get("pagemap"), Map.class);
							MainDto dto = new MainDto();
							dto.setKeyword(request_param.get("query"));
							dto.setPortal(request_param.get("portal").toUpperCase());
							dto.setAuthor(map.get("videoobject").get(0).get("channelid"));
							dto.setPublished_date(map.get("videoobject").get(0).get("uploaddate"));
							dto.setThumbnail(map.get("videoobject").get(0).get("thumbnailurl"));
							dto.setTitle(map.get("metatags").get(0).get("title"));
							dto.setDescription(map.get("metatags").get(0).get("og:description"));
							dto.setLink(map.get("metatags").get(0).get("og:url"));
							dto.setStart_index(String.valueOf(request.get("startIndex")));
							log.info(dto.toString());
//							dto_list.add(dto);
						});
			} else {
				throw new Exception("검색 결과가 없습니다.");
			}
		}

		return result_list;
	}*/

	@Override
	public List<Map<String, Object>> selectDaumBlog(Map<String, String> request_param) throws Exception{
		Map<String, String> fields_map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		PlaceDetailDto place_detail_dto = new PlaceDetailDto();
		place_detail_dto.setPlace_id(request_param.get("place_id"));
		Map<String, String> place_detail_map = mapper.convertValue(selectPlaceDetail(place_detail_dto), Map.class);
		log.info(place_detail_map.toString());

		fields_map.put("query", request_param.get("query"));
		fields_map.put("place_id", request_param.get("place_id"));
		fields_map.put("place_name", place_detail_map.get("place_name"));
		fields_map.put("user_id", request_param.getOrDefault("user_id", "temp"));
		fields_map.put("sns_division", request_param.getOrDefault("sns_division", "T"));
		fields_map.put("size", request_param.getOrDefault("size", ""));

		int count = place_repo.selectDaumBlogsCount(fields_map);
		if(count < 1) {
			String daum_blog_string = apiService.daumSearchBlog(fields_map);
			log.info(daum_blog_string);

			List<Map<String, String>> daum_blog_list = JsonPath.parse(daum_blog_string).read("$.documents");

			daum_blog_list = daum_blog_list.stream()
					.filter(map -> map.get("url").contains("tistory"))
					.peek(map -> {
						map.replace("title", map.get("title").replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""));
						map.replace("title", map.get("title").replaceAll("&#39;", "'"));
						map.replace("contents", map.get("contents").replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", ""));
						map.replace("contents", map.get("contents").replaceAll("&#39;", "'"));
						map.replace("contents", map.get("contents").replaceAll("&lt;", "<"));
						map.replace("contents", map.get("contents").replaceAll("&gt;", ">"));
						map.replace("contents", map.get("contents").replaceAll("&quot;", "\""));
						map.replace("contents", map.get("contents").replaceAll("&amp;", "&"));
						log.info(map.toString());
					})
					.collect(Collectors.toList());

			place_repo.insertDaumBlogs(daum_blog_list, fields_map);
		}

		List<Map<String, Object>> result_list = place_repo.selectDaumBlogs(fields_map);
		List<Map<String, Object>> return_list = new ArrayList<>();
		for(Map<String, Object> result_map : result_list){
			Map<String, Object> temp_map = new LinkedHashMap<>();
			temp_map.put("index", result_map.get("row_number"));
			temp_map.put("review_id", result_map.get("review_id"));
			temp_map.put("title", result_map.get("title"));
			temp_map.put("write_date", result_map.get("write_date"));
			temp_map.put("author", result_map.get("author"));
			temp_map.put("description", result_map.get("description"));
			temp_map.put("url", result_map.get("url"));
			temp_map.put("thumbnail_url", result_map.get("thumbnail_url"));
			if(Integer.parseInt(result_map.get("bookmark_flag").toString()) > 0)
				temp_map.put("bookmark_flag", true);
			else
				temp_map.put("bookmark_flag", false);

			return_list.add(temp_map);
		}

		return return_list;
	}

	@Override
	public List<Map<String, Object>> selectYoutubeReview(Map<String, String> request_param) throws Exception{
		Map<String, String> fields_map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		PlaceDetailDto place_detail_dto = new PlaceDetailDto();
		place_detail_dto.setPlace_id(request_param.get("place_id"));
		Map<String, String> place_detail_map = mapper.convertValue(selectPlaceDetail(place_detail_dto), Map.class);
		log.info(place_detail_map.toString());

		fields_map.put("query", request_param.get("query"));
		fields_map.put("place_id", request_param.get("place_id"));
		fields_map.put("place_name", place_detail_map.get("place_name"));
		fields_map.put("user_id", request_param.getOrDefault("user_id", "temp"));
		fields_map.put("sns_division", request_param.getOrDefault("sns_division", "T"));
		fields_map.put("size", request_param.getOrDefault("size", ""));

		int count = place_repo.selectYoutubeReviewsCount(fields_map);
		if(count < 1) {
			String youtube_review_string = apiService.youtubeSearchReviews(fields_map);
			List<Map<String, String>> youtube_review_list = JsonPath.parse(youtube_review_string).read("$.documents");

			youtube_review_list = youtube_review_list.stream()
					.filter(map -> map.get("url").contains("youtube"))
					.peek(map -> {
//						try {
////							map.put("thumbnail",getOpenGraph(map.get("url")));
////						} catch (IOException e) {
////							e.printStackTrace();
////						}
						log.info(map.toString());
					})
					.collect(Collectors.toList());

			place_repo.insertYoutubeReviews(youtube_review_list, fields_map);
		}

		List<Map<String, Object>> result_list = place_repo.selectYoutubeReviews(fields_map);
		List<Map<String, Object>> return_list = new ArrayList<>();
		for(Map<String, Object> result_map : result_list){
			Map<String, Object> temp_map = new LinkedHashMap<>();
			temp_map.put("index", result_map.get("row_number"));
			temp_map.put("review_id", result_map.get("review_id"));
			temp_map.put("title", result_map.get("y_title"));
			temp_map.put("write_date", result_map.get("write_date"));
			temp_map.put("author", result_map.get("author"));
			temp_map.put("url", result_map.get("y_video_id"));
			temp_map.put("thumbnail_url", result_map.get("y_thumbnail_url"));
			if(Integer.parseInt(result_map.get("bookmark_flag").toString()) > 0)
				temp_map.put("bookmark_flag", true);
			else
				temp_map.put("bookmark_flag", false);

			return_list.add(temp_map);
		}

		return return_list;
	}

	@Override
	public List<Map<String, Object>> selectGoogleReview(Map<String, String> request_param) throws Exception{
		Map<String, String> fields_map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		PlaceDetailDto place_detail_dto = new PlaceDetailDto();
		place_detail_dto.setPlace_id(request_param.get("place_id"));
		Map<String, String> place_detail_map = mapper.convertValue(selectPlaceDetail(place_detail_dto), Map.class);
		log.info(place_detail_map.toString());

		fields_map.put("query", request_param.get("query"));
		fields_map.put("place_id", request_param.get("place_id"));
		fields_map.put("google_place_id", place_detail_map.get("google_place_id"));
		fields_map.put("place_name", place_detail_map.get("place_name"));
		fields_map.put("user_id", request_param.getOrDefault("user_id", "temp"));
		fields_map.put("sns_division", request_param.getOrDefault("sns_division", "T"));
		fields_map.put("size", request_param.getOrDefault("size", ""));

		int count = place_repo.selectGoogleReviewsCount(fields_map);
		if(count < 1) {
			String google_review_string = apiService.GoogleSearchReviews(fields_map);
			String status = JsonPath.parse(google_review_string).read("$.status");

			if(!"INVALID_REQUEST".equals(status)) {
				List<Map<String, String>> google_review_list = JsonPath.parse(google_review_string).read("$[?(@.result.reviews)].result.reviews[*]");
//				List<Map<String, String>> google_review_list = JsonPath.parse(google_review_string).read("$.result.reviews");
				google_review_list = google_review_list.stream()
						.peek(map -> {
//						map.replace("text", map.get("text").replaceAll("\n", ""));
							log.info(map.toString());
						})
						.collect(Collectors.toList());

				place_repo.insertGoogleReviews(google_review_list, fields_map);
			}
		}

		List<Map<String, Object>> result_list = place_repo.selectGoogleReviews(fields_map);
		List<Map<String, Object>> return_list = new ArrayList<>();
		for(Map<String, Object> result_map : result_list){
			Map<String, Object> temp_map = new LinkedHashMap<>();
			temp_map.put("index", result_map.get("row_number"));
			temp_map.put("review_id", result_map.get("review_id"));
			temp_map.put("author", result_map.get("author"));
			temp_map.put("g_content", result_map.get("g_content"));
			temp_map.put("g_rating", result_map.get("g_rating"));
			temp_map.put("write_date", result_map.get("write_date"));

			return_list.add(temp_map);
		}

		return return_list;
	}

	public List<Map<String, Object>> selectEatzeongReview(Map<String, String> request_param) throws Exception{
		List<Map<String, Object>> result_list = place_repo.selectEatzeongReviews(request_param);
		List<Map<String, Object>> return_list = new ArrayList<>();
		for(Map<String, Object> result_map : result_list){
			Map<String, Object> temp_map = new LinkedHashMap<>();
			temp_map.put("index", result_map.get("row_number"));
			temp_map.put("review_id", result_map.get("review_id"));
			temp_map.put("review_user_id", result_map.get("review_user_id"));
			temp_map.put("author", result_map.get("author"));
			temp_map.put("profile_image_url", HOST_URL + "/profile/" + result_map.get("profile_image_url"));
			temp_map.put("rating_point", result_map.get("rating_point"));
			temp_map.put("review_contents", result_map.get("review_contents"));
			temp_map.put("like_count", result_map.get("like_count"));
			temp_map.put("write_date", result_map.get("add_date"));
			if(result_map.get("attach_number") != null)
				temp_map.put("image_url", selectEatzeongReviewAttachments(result_map));
			else
				temp_map.put("image_url", new ArrayList<>());

			return_list.add(temp_map);
		}
		return return_list;
	}

	private List<String> selectEatzeongReviewAttachments(Map<String, Object> result_map) throws Exception{
		List<String> return_list = new ArrayList<>();
		List<Map<String, Object>> attachment_list = place_repo.selectEatzeongReviewAttachments(result_map);
		for(Map<String, Object> attachment : attachment_list){
			return_list.add(HOST_URL + "/review/" + attachment.get("attach_name"));
		}
		return return_list;
	}

	private String getOpenGraph(String url) throws IOException {

		Document doc = Jsoup.connect(url).get();
		String thumbnail_url = doc.select("meta[property=og:image]").attr("content");

		log.info("url : {}", url);
		log.info("genre : {}", doc.select("meta[itemprop=genre]").attr("content"));
		log.info("thumbnail_url : {}", thumbnail_url);

		return thumbnail_url;
	}

	@Override
	public void insertBlacklist(Map<String, String> request_param) throws Exception{

		List<Map<String, Object>> blacklist_list = new ArrayList<>();

		log.info(request_param.toString());
		place_repo.deleteBlacklist(request_param);						// blacklist_division가 post인 경우 blacklist_division가 author이고 portal, author가 같은 데이터 모두 delete
		int count = place_repo.selectBlacklistCount(request_param);		// author인 경우 blacklist_division가 post이고 portal, author가 같은 데이터 모두 delete
		if(count < 1)
			place_repo.insertBlacklist(request_param);
		else
			throw new ExistException("이미 존재하는 데이터입니다.");
	}

	@Override
	public void deleteBlacklistOne(Map<String, String> request_param) throws Exception{
		place_repo.deleteBlacklistOne(request_param);
	}

	@Override
	public Map<String, Object> selectBlacklistFlag(Map<String, String> request_param) throws Exception{
		Map<String, Object> return_map = new LinkedHashMap<>();
		Map<String, Object> result_map = new LinkedHashMap<>();
		result_map = place_repo.selectBlacklistFlag(request_param);
		if(!MapUtils.isEmpty(result_map)) {
			if ("post".equals(result_map.get("blacklist_division"))) {
				return_map.put("blacklist_author_flag", false);
				return_map.put("blacklist_post_flag", true);
			} else {
				return_map.put("blacklist_author_flag", true);
				return_map.put("blacklist_post_flag", false);
			}
		}else{
			return_map.put("blacklist_author_flag", false);
			return_map.put("blacklist_post_flag", false);
		}

		return return_map;
	}

	@Override
	public Map<String, Object> selectBlacklist(Map<String, String> request_param) throws Exception{
		Map<String, Object> return_map = new LinkedHashMap<>();
		List<Map<String, Object>> blacklist_author_list = new ArrayList<>();
		List<Map<String, Object>> blacklist_post_list = new ArrayList<>();
		List<Map<String, Object>> blacklist_post_list_youtube = new ArrayList<>();
		List<Map<String, Object>> blacklist_post_list_tistory = new ArrayList<>();
		List<Map<String, Object>> blacklist_post_list_naver = new ArrayList<>();
		List<Map<String, Object>> blacklist_author_list_youtube = new ArrayList<>();
		List<Map<String, Object>> blacklist_author_list_tistory = new ArrayList<>();
		List<Map<String, Object>> blacklist_author_list_naver = new ArrayList<>();

		blacklist_post_list = place_repo.selectBlacklistPost(request_param);
		blacklist_author_list = place_repo.selectBlacklistAuthor(request_param);

		for (Map<String, Object> temp_map : blacklist_post_list) {
			Map<String, Object> map = new LinkedHashMap<>();

			map.put("review_id", temp_map.get("review_id"));
			map.put("title", temp_map.get("title"));
			map.put("description", temp_map.getOrDefault("description", ""));
			map.put("author", temp_map.get("author"));
			map.put("url", temp_map.get("url"));
			map.put("thumbnail_url", temp_map.getOrDefault("thumbnail_url", ""));
			map.put("write_date", temp_map.get("write_date"));
			map.put("add_date", temp_map.get("add_date"));

			if (temp_map.get("portal").equals("YOUTUBE")) {
				blacklist_post_list_youtube.add(map);
			} else if (temp_map.get("portal").equals("NAVER")) {
				blacklist_post_list_naver.add(map);
			} else {
				blacklist_post_list_tistory.add(map);
			}
		}

		for (Map<String, Object> temp_map : blacklist_author_list) {
			Map<String, Object> map = new LinkedHashMap<>();

			map.put("author", temp_map.get("author"));
			map.put("add_date", temp_map.get("add_date"));

			if (temp_map.get("portal").equals("YOUTUBE")) {
				blacklist_author_list_youtube.add(map);
			} else if (temp_map.get("portal").equals("NAVER")) {
				blacklist_author_list_naver.add(map);
			} else {
				blacklist_author_list_tistory.add(map);
			}
		}

		return_map.put("author_blacklist_youtube", blacklist_author_list_youtube);
		return_map.put("author_blacklist_naver", blacklist_author_list_naver);
		return_map.put("author_blacklist_tistory", blacklist_author_list_tistory);

		return_map.put("post_blacklist_youtube", blacklist_post_list_youtube);
		return_map.put("post_blacklist_naver", blacklist_post_list_naver);
		return_map.put("post_blacklist_tistory", blacklist_post_list_tistory);

		return return_map;
	}
}
