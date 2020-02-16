package com.place.service;

import com.google.common.collect.*;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.place.api.google.GoogleCustomSearch;
import com.place.dto.MainDto;
import com.place.dto.PlaceDto;
import com.place.repository.CommonRepository;
import com.place.service.interfaces.CommonServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service("com.place.service.CommonService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
@Slf4j
public class CommonService implements CommonServiceInterface {
    //	private final String HOST_URL = "http://api.matitzung.shop";				// 개발서버
    private final String HOST_URL = "http://localhost:8080";                    // 로컬서버

    @Inject
    CommonRepository common;

    @Inject
    GoogleCustomSearch customSearch;

    @Override
    public Map<String, Object> selectArea() throws Exception {
        BiMap<String, String> bimap_column = HashBiMap.create();
        SortedSetMultimap<String, Comparable> multimap = MultimapBuilder.linkedHashKeys().treeSetValues().build();
        List<String> large_category = Lists.newArrayList();
        List<Object> medium_category = Lists.newArrayList();
        Map<String, Object> result_map = Maps.newLinkedHashMap();


        for (Map<String, String> map : common.selectArea()) {
            if (map.get("parent_area").equals("*")) {
                bimap_column.put(map.get("area_name"), map.get("area"));
                large_category.add(map.get("area_name"));
            } else {
                multimap.put(bimap_column.inverse().get(map.get("parent_area")), map.get("area_name"));
            }
        }

        System.out.println(multimap);
        Iterator<String> itr = multimap.asMap().keySet().iterator();
        while (itr.hasNext()) {
            Map<String, List<String>> area_map = Maps.newLinkedHashMap();
            List<String> value_list = Lists.newArrayList();
            String key = (String) itr.next();
            multimap.get(key).forEach(value -> {
                value_list.add(value.toString());
            });
            area_map.put(key, value_list);
            medium_category.add(area_map);
        }

        result_map.put("large_category", large_category);
        result_map.put("medium_category", medium_category);

        return result_map;
    }

    @Override
    public List<String> suggestKeyword(String keyword) throws Exception {
        return common.selectSuggestKeyword(keyword);
    }

    /**
     * 북마크 조회
     */
    public List<Map<String, Object>> selectBookmarks(Map<String, String> allRequestParams) throws Exception {
        List<Map<String, Object>> query_list = Lists.newArrayList();
        List<Map<String, Object>> result_list = Lists.newArrayList();
        log.info("Parameters : " + allRequestParams.toString());

        try {
            query_list = common.selectBookmarks(allRequestParams);
            log.debug(query_list.toString());

            if (query_list.size() < 1) {
//                Map<String, Object> return_map = Maps.newHashMap();
//                return_map.put("result_count", 0);
//                return_map.put("result_message", "검색결과 없음");
//                result_list.add(null);
                return null;
            } else {
                result_list = parseBookmarks(query_list, allRequestParams.get("gubun"));
                return result_list;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> insertBookmarks(Map<String, String> allRequestParams) throws Exception {
        Map<String, Object> return_map = Maps.newHashMap();
        log.info("Parameters : " + allRequestParams.toString());

        int count = common.selectBookmarkCnt(allRequestParams);
        log.debug("북마크 갯수 : " + count);
        if (count < 1) {
            common.insertBookmarks(allRequestParams);
            return_map.put("result_message", "insert 성공");
        } else {
            return_map.put("result_message", "insert 실패. 해당 데이터의 북마크가 이미 존재합니다.");
        }

        return return_map;
    }

    @Override
    public Map<String, Object> deleteBookmark(Map<String, String> allRequestParams) throws Exception {
        Map<String, Object> return_map = Maps.newHashMap();
        log.info("Parameters : " + allRequestParams.toString());

        try {
            common.deleteBookmarks(allRequestParams);
            return_map.put("result_message", "delete 성공");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        return return_map;
    }

    @Override
    public List<String> getCustomSearch(String query, String portal) throws Exception {

        List<String> request_url_list = Lists.newArrayList();
        String key = "AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc";

        Map<String, String> search_cx = Maps.newHashMap();

        search_cx.put("NAVER", "007124061159672905157:4dldrdpppep");
        search_cx.put("DAUM", "007124061159672905157:eoo651giqr9");
        search_cx.put("YOUTUBE", "007124061159672905157:ceyxyhcsbvt");
        int start_index = 1;

        for (String search_cx_key : search_cx.keySet()) {
            if (search_cx_key.equalsIgnoreCase(portal)) {
                for (int i = 0; i < 3; i++) {
                    String request_url = "https://www.googleapis.com/customsearch/v1/siterestrict?q={query}&sort=date:r:{date}&key={key}&cx={cx}&start={startindex}";
                    Map<String, String> temp_map = Maps.newHashMap();
                    temp_map.put("cx", search_cx.get(search_cx_key));
                    temp_map.put("key", key);
                    temp_map.put("query", query);
                    temp_map.put("startindex", Integer.toString(start_index));
                    if (search_cx_key.equalsIgnoreCase("NAVER")) {
                        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        String last_year_date = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        temp_map.put("date", last_year_date + ":" + today); // 최근 6개월
                    } else if (search_cx_key.equalsIgnoreCase("DAUM")) {
                        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        String last_year_date = LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        temp_map.put("date", last_year_date + ":" + today); // 최근 1년
                    } else {
                        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        String last_year_date = LocalDateTime.now().minusYears(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                        temp_map.put("date", last_year_date + ":" + today); // 최근 2년
                    }

                    request_url = request_url.replace("{query}", temp_map.get("query"));
                    request_url = request_url.replace("{date}", temp_map.get("date"));
                    request_url = request_url.replace("{key}", temp_map.get("key"));
                    request_url = request_url.replace("{cx}", temp_map.get("cx"));
                    request_url = request_url.replace("{startindex}", temp_map.get("startindex"));
                    start_index += 10;
                    request_url_list.add(request_url);
                }
            }
        }

        return request_url_list;
    }

    private List<Map<String, Object>> parseBookmarks(List<Map<String, Object>> query_list, String gubun) throws Exception {
        List<Map<String, Object>> result_list = Lists.newArrayList();

        if (gubun.equals("youtube")) {
            for (Map<String, Object> map : query_list) {
                Map<String, Object> return_map = Maps.newHashMap();

//                return_map.put("result_count", query_list.size());
                return_map.put("place_id", map.get("place_id"));
                return_map.put("gubun", map.get("gubun"));
                return_map.put("place_name", map.get("place_name"));
                return_map.put("review_id", map.get("review_id"));
                return_map.put("write_date", map.get("write_date").toString());
                return_map.put("title", map.get("y_title"));
                return_map.put("description", map.get("y_description"));
                return_map.put("url", map.get("y_video_id"));
                return_map.put("thumbnail_url", map.get("y_thumbnail_url"));

                result_list.add(return_map);
            }
        } else if (gubun.equals("naver") || gubun.equals("tistory")) {
            for (Map<String, Object> map : query_list) {
                Map<String, Object> return_map = Maps.newHashMap();

//                return_map.put("result_count", query_list.size());
                return_map.put("place_id", map.get("place_id"));
                return_map.put("gubun", map.get("gubun"));
                return_map.put("place_name", map.get("place_name"));
                return_map.put("review_id", map.get("review_id"));
                return_map.put("author", map.get("author"));
                return_map.put("write_date", map.get("write_date").toString());
                return_map.put("url", map.get("url"));
                return_map.put("title", map.get("title"));
                return_map.put("description", map.get("description"));
                return_map.put("thumbnail_url", map.get("thumbnail_url"));

                result_list.add(return_map);
            }
        } else if (gubun.equals("app")) {
            for (Map<String, Object> map : query_list) {
                Map<String, Object> return_map = Maps.newHashMap();

//                return_map.put("result_count", query_list.size());
                return_map.put("place_id", map.get("place_id"));
                return_map.put("gubun", map.get("gubun"));
                return_map.put("place_name", map.get("place_name"));
                return_map.put("review_id", map.get("review_id"));
                return_map.put("write_date", map.get("add_date").toString());
                return_map.put("write_time", map.get("add_time"));
                return_map.put("review_contents", map.get("review_contents"));
                return_map.put("like_count", map.get("like_count"));
                return_map.put("like_flag", map.get("like_flag"));
                return_map.put("rating_point", String.format("%d", map.get("rating_point")));
                return_map.put("write_user_id", map.get("review_user_id"));

                result_list.add(return_map);
            }
        } else {
            for (Map<String, Object> map : query_list) {
                Map<String, Object> return_map = Maps.newHashMap();

//                return_map.put("result_count", query_list.size());
                return_map.put("place_id", map.get("place_id"));
                return_map.put("gubun", map.get("gubun"));
                return_map.put("place_name", map.get("place_name"));
                if (!(map.get("open_hours") == null || map.get("open_hours") == ""))
                    return_map.put("open_hours", map.get("open_hours"));
                else
                    return_map.put("open_hours", "");

                if (!(map.get("rating_point") == null || map.get("rating_point") == ""))
                    return_map.put("rating_point", String.format("%.1f", map.get("rating_point")));
                else
                    return_map.put("rating_point", "");

                if (!(map.get("thumbnail") == null || map.get("thumbnail") == ""))
                    return_map.put("thumbnail", HOST_URL + "/review/" + map.get("thumbnail"));
                else
                    return_map.put("thumbnail", "");

                result_list.add(return_map);
            }
        }

        return result_list;
    }

    /**
     * DB에 데이터 유무 확인
     *
     * @param request_param
     * @return 조회 결과 갯수
     */
    public int selectMainCount(Map<String, String> request_param) throws Exception {
        return common.selectMainCount(request_param);
    }

    /**
     * main 데이터 Insert
     *
     * @param request_param
     * @throws Exception
     */
    public void insertCustomSearch(Map<String, String> request_param) throws Exception {

        List<MainDto> main_dto_list = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {
            Map<String, String> fields = Maps.newHashMap();
            fields.put("q", request_param.get("query"));
            fields.put("start", Integer.toString(i * 10 + 1));

            if (request_param.get("portal").equalsIgnoreCase("NAVER")) {
                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String last_year_date = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 6개월
            } else if (request_param.get("portal").equalsIgnoreCase("DAUM")) {
                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String last_year_date = LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 1년
            } else {
                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String last_year_date = LocalDateTime.now().minusYears(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                fields.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 2년
            }

            String jsonStr = customSearch.callApi(customSearch.CreateURL(fields), request_param.get("portal"));
            DocumentContext document = JsonPath.parse(jsonStr);
            main_dto_list = parseItem(document, request_param);
            common.insertCustomSearch(main_dto_list);
        }

    }

	private List<MainDto> parseItem(DocumentContext document, Map<String, String> request_param) throws Exception {
		List<MainDto> dto_list = new ArrayList<>();
        ObjectMapper oMapper = new ObjectMapper();

        if(request_param.get("portal").equalsIgnoreCase("NAVER")) {
            SimpleDateFormat origin_format = new SimpleDateFormat("yyyy년 M월 d일");
            SimpleDateFormat new_format = new SimpleDateFormat("yyyy-MM-dd");

            Map<String, String> request = (Map<String, String>) document.read("$.NAVER.queries.request[0]");

            if (Integer.parseInt(request.get("totalResults")) > 0) {
                List<Map<String, Object>> items = document.read("$.NAVER.items[*]");
                items.stream()
                        .filter(item -> item.get("snippet").toString().indexOf("일") < 15)
                        .forEach(item -> {
                            Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
                            MainDto dto = new MainDto();
                            dto.setKeyword(request_param.get("query"));
                            dto.setPortal(request_param.get("portal").toUpperCase());
                            dto.setThumbnail(map.get("cse_image").get(0).get("src"));
                            dto.setTitle(map.get("metatags").get(0).get("og:title"));
                            dto.setDescription(map.get("metatags").get(0).get("og:description"));
                            dto.setLink(map.get("metatags").get(0).get("og:url"));
                            dto.setAuthor(map.get("metatags").get(0).get("naverblog:nickname"));
                            dto.setStart_index(String.valueOf(request.get("startIndex")));
                            if(item.get("snippet").toString().indexOf("일")+1 > 10){
                                String published_date = item.get("snippet").toString().substring(0, item.get("snippet").toString().indexOf("일") + 1);
                                try {
                                    published_date = LocalDate.parse(new_format.format(origin_format.parse(published_date))).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    dto.setPublished_date(published_date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                dto.setPublished_date("1900-01-01");
                            }

                            log.info(dto.toString());
                            dto_list.add(dto);
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
                            Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
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
                            Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
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
                            dto_list.add(dto);
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
                            Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
                            return map.containsKey("videoobject")
                                        && map.get("videoobject").get(0).get("genre").equalsIgnoreCase("People & Blogs");
                        })
                        .forEach(item -> {
                            Map<String, List<Map<String, String>>> map = oMapper.convertValue(item.get("pagemap"), Map.class);
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
                            dto_list.add(dto);
                        });
            } else {
                throw new Exception("검색 결과가 없습니다.");
            }
        }

    	return dto_list;
	}

    public List<Map<String, String>> selectMain(Map<String, String> request_param) throws Exception {
        List<Map<String, String>> return_data = new ArrayList<>();
        List<MainDto> data_dto = new ArrayList<>();

        request_param.put("size", request_param.getOrDefault("size", ""));
        data_dto = common.selectMain(request_param);
        long data_count = data_dto.stream()
                .filter(dto -> !dto.getTitle().isBlank())
                .sorted(Comparator.comparing(MainDto::getStart_index))
                .peek(dto -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("author", dto.getAuthor());
                    map.put("title", dto.getTitle());
                    map.put("description", dto.getDescription());
                    if(dto.getThumbnail() == null || dto.getThumbnail().isBlank())
                        map.put("thumbnail_url", null);
                    else
                        map.put("thumbnail_url", dto.getThumbnail());
                    map.put("url", dto.getLink());
                    map.put("write_date", dto.getPublished_date());
                    map.put("start_index", dto.getStart_index());
                    return_data.add(map);
                })
                .count();

        log.info("검색 개수 : {}", data_count);

        return return_data;
    }

    @Override
    public List<Map<String, String>> selectFirstArea() throws Exception{
        return common.selectFirstArea();
    }

    @Override
    public List<Map<String, String>> selectSecondArea(String area) throws Exception{
        return common.selectSecondArea(area).stream()
                .sorted(Comparator.comparing(res -> res.get("area")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Object> selectSuggestArea() throws Exception{
        return common.selectSuggestArea();
    }

    @Override
    public int selectMainPlacesCount(PlaceDto place_dto) throws Exception{
        return common.selectMainPlacesCount(place_dto);
    }

    @Override
    public List<Map<String, Object>> selectMainPlaces(PlaceDto place_dto) throws Exception{
        List<Map<String, Object>> return_list = new ArrayList<>();

        List<Map<String, Object>> result_list = common.selectMainPlaces(place_dto);
        result_list.forEach(result_map -> {
            Map<String, Object> temp_map = new LinkedHashMap<>();
            temp_map.put("place_id", result_map.get("place_id"));
            temp_map.put("place_name", result_map.get("place_name"));
            temp_map.put("open_hours", result_map.get("open_hours"));
            temp_map.put("category_name", result_map.get("category_name"));
            temp_map.put("latitude", result_map.get("latitude"));
            temp_map.put("longitude", result_map.get("longitude"));
            temp_map.put("google_rating", result_map.get("google_rating"));
            temp_map.put("appreview_rating", result_map.get("appreview_rating"));
            temp_map.put("place_address", result_map.get("place_address"));
            temp_map.put("road_place_address", result_map.get("road_place_address"));
            if(result_map.get("blog_thumbnail") == null || result_map.get("blog_thumbnail").toString().isBlank())
                temp_map.put("blog_thumbnail", null);
            else
                temp_map.put("blog_thumbnail", result_map.get("blog_thumbnail"));
            temp_map.put("app_thumbnail", result_map.get("app_thumbnail"));

            return_list.add(temp_map);
        });

        return return_list;
    }

    @Override
    public Map<String, Object> selectBookmarkFlag(Map<String, String> allRequestParams) throws Exception {
        Map<String, Object> return_map = new LinkedHashMap<>();

        int count = common.selectBookmarkFlag(allRequestParams);
        if(count > 0) {
            return_map.put("result_flag", true);
            return_map.put("result_message", "북마크가 존재합니다.");
        }
        else {
            return_map.put("result_flag", false);
            return_map.put("result_message", "북마크가 없습니다.");
        }


        return return_map;
    }
}


