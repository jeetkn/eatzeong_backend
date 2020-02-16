package com.place.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.place.api.google.GoogleCustomSearch;
import com.place.dto.Dto;
import com.place.dto.PlaceDto;
import com.place.service.CommonService;
import com.place.service.MyService;
import com.place.service.PlaceService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
@Slf4j
public class CommonController {
    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);
    static final String URL1 = "http://localhost:8090/service1?req={req}";
    static final String URL2 = "http://localhost:8090/service2?req={req}";
    WebClient webClient = WebClient.create();

    @Resource(name = "com.place.service.CommonService")
    CommonService common;

    @Resource(name = "com.place.service.PlaceService")
    PlaceService placeService;

    @Resource(name = "com.place.service.MyService")
    MyService myService;

    @GetMapping("/rest")
    public Flux<String> rest(String query, String portal) throws Exception {
//        Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange();
//        Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
//        return body;

        // ClientResponse는 ResponseEntity와 유사
		/*Mono<ClientResponse> resMono = client.get()
						.uri(URL1, idx)
						.exchange();
		// 여기까지만으로는 파이프라인만 구성되며 실제 호출은 하지 않음(Stream과 유사)
		// publisher는 subscriber가 subscribe를 해야만 파이프라인을 실행해서 publish 한다.
		// return 타입이 Mono이면 Spring이 subscribe를 해서 파이프라인이 실행된다.

		// ClientResponse의 Body만 String으로 빼오기
		Mono<String> resultMono = client.get()
				.uri(URL1, idx)
				.exchange()
				.flatMap(  															// bodyToMono가 Mono<T>를 반환하므로 flatMap을 써야함
						clientResponse -> clientResponse.bodyToMono(String.class))  // 결과 Mono<String>
				.doOnNext(c -> log.info(c))  										// log로 Thread 이름 확인 - A
				// 아래와 같이 flatMap()을 통해 chaining하면 nonblocking-async를 순차적으로 처리 가능
				.flatMap(res1 -> client.get().uri(URL2, res1).exchange())  			// 결과 Mono<ClientResponse>
				.flatMap(c -> c.bodyToMono(String.class))  							// 결과 Mono<String>
				.doOnNext(c -> log.info(c))  										// log로 Thread 이름 확인 - A
				.flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2)))  	// 결과 Mono<String>
				.doOnNext(c -> log.info(c))
				.log();  										// myService.work()를 실행하는 Thread 이름 확인 - B

		return resultMono;*/

//		Mono<String> m = myService.async(idx);
        String URL = "https://www.googleapis.com/customsearch/v1/siterestrict?q=홍대 맛집&sort=date:r:20180130:20200130&key=AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc&cx=007124061159672905157:4dldrdpppep";

        List<String> naver_custom_search = common.getCustomSearch(query, "NAVER");
        List<String> youtube_custom_search = common.getCustomSearch(query, "YOUTUBE");
        List<String> daum_custom_search = common.getCustomSearch(query, "DAUM");

        log.info(naver_custom_search.toString());
        log.info(youtube_custom_search.toString());
        log.info(daum_custom_search.toString());

        Mono<String> m = webClient.get().uri(URL)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2)))
                .flatMap(s -> Mono.fromCompletionStage(CompletableFuture.completedFuture(s)))
                .log();

        Flux<String> flux = webClient.get().uri(URL)
//				.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(c -> log.info(c.toString()));


        return flux;

//		return myService.async(query);
    }

    @GetMapping(value = "/main/reviews")
    public Dto<List<Map<String, String>>> mainReviews(@RequestParam Map<String, String> request_param) {
        Dto<List<Map<String, String>>> return_dto = new Dto<>();
        Map<String, Object> return_map = Maps.newHashMap();

        try {
            if (!request_param.containsKey("query") || !request_param.containsKey("portal"))
                throw new Exception("Invalid parameter");
            if (request_param.get("query") == null || request_param.get("query").isBlank())
                throw new Exception("query 파라미터를 확인해주세요.");
            if (request_param.get("portal") == null || request_param.get("portal").isBlank())
                throw new Exception("portal 파라미터를 확인해주세요.");

            request_param.put("portal", request_param.get("portal").toUpperCase());
            int count = common.selectMainCount(request_param);
            if (count < 1) {
                common.insertCustomSearch(request_param);
            }

            return_dto.setDataList(common.selectMain(request_param));

        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, String>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    @GetMapping(value = "/main/places")
    public Dto<List<Map<String, Object>>> mainPlaces(@RequestParam Map<String, String> request_param,
                                                     PlaceDto place_dto) {
        Dto<List<Map<String, Object>>> return_dto = new Dto<>();

        try {
            if (!request_param.containsKey("query"))
                throw new Exception("Invalid parameter");
            if (request_param.get("query") == null || request_param.get("query").isBlank())
                throw new Exception("query 파라미터를 확인해주세요.");

            place_dto.setKeyword(request_param.get("query"));
            if(place_dto.getSize() == null || place_dto.getSize().isBlank())
                place_dto.setSize("0");
            log.info(place_dto.toString());
            int count = common.selectMainPlacesCount(place_dto);
            if(count < 1)
                placeService.selectPlaceList(place_dto);

            return_dto.setDataList(common.selectMainPlaces(place_dto));

        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    @GetMapping(value = "/area")
    public Map<String, Object> getArea() {
        Map<String, Object> multimap = Maps.newHashMap();
        try {
            multimap = common.selectArea();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return multimap;
    }

    @GetMapping(value = "/area/first")
    public Dto<List<Map<String, String>>> getFirstArea(){
        Dto<List<Map<String, String>>> return_dto = new Dto<>();
        Map<String, String> return_map = Maps.newHashMap();

        try {
            return_dto.setDataList(common.selectFirstArea());
        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, String>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    @GetMapping(value = "/area/second")
    public Dto<List<Map<String, String>>> getSecondArea(@RequestParam String area){
        Dto<List<Map<String, String>>> return_dto = new Dto<>();
        Map<String, String> return_map = Maps.newHashMap();

        try {
            return_dto.setDataList(common.selectSecondArea(area));
        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, String>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }

    /**
     * 지역 검색
     * @return
     */
    @GetMapping(value = "/area/suggest")
    public Dto<List<Object>> getSuggestArea(){
        Dto<List<Object>> return_dto = new Dto<>();
        Map<String, String> return_map = new HashMap<>();

        try {
            return_dto.setDataList(common.selectSuggestArea());
        }catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dto.setDataList(Arrays.asList(error));
            return return_dto;
        }

        return return_dto;
    }




    @GetMapping(value = "/autocomplete")
    public List<String> selectKeyword(@RequestParam("term") String keyword) {
        List<String> list = Lists.newArrayList();
        try {
            list = common.suggestKeyword(keyword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 북마크 리스트 조회
     *
     * @param allRequestParams
     * @return
     */
    @GetMapping(value = "/bookmarks")
    public Dto<List<Map<String, Object>>> selectBookmark(
//			@RequestParam(required = false) String place_id,
//			@RequestParam(required = false) String user_id,
//			@RequestParam(required = false, defaultValue = "place") String gubun		// place, youtube, naver, tistory
            @RequestParam Map<String, String> allRequestParams) {

        Dto<List<Map<String, Object>>> return_dto = new Dto<List<Map<String, Object>>>();
        List<Map<String, Object>> return_dataList = Lists.newArrayList();
        Map<String, Object> return_map = Maps.newHashMap();

        List<String> gubun_list = Lists.newArrayList("place", "naver", "youtube", "tistory", "app");

        try {

            if (!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id") == null || allRequestParams.get("user_id").isBlank()) {
                return_dto.setCode(400);
                return_dto.setMessage("user_id는 필수 파라미터입니다. user_id를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dataList.add(return_map);
                return_dto.setDataList(return_dataList);
                return return_dto;
            }
            if (!allRequestParams.containsKey("gubun") || allRequestParams.get("gubun") == null || allRequestParams.get("gubun").isBlank()) {    // place, youtube, naver, tistory, app
                allRequestParams.putIfAbsent("gubun", "place");
            }
            if (!gubun_list.contains(allRequestParams.get("gubun"))) {        //gubun 값이 gubun_list 안에 없을 경우
                return_dto.setCode(400);
                return_dto.setMessage("gubun 파라미터를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dataList.add(return_map);
                return_dto.setDataList(return_dataList);
                return return_dto;
            }

            return_dto.setDataList(common.selectBookmarks(allRequestParams));

        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_dataList.add(error);
            return_dto.setDataList(return_dataList);
            return return_dto;
        }

        return return_dto;
    }


    /**
     * 북마크 생성
     *
     * @param allRequestParams
     * @return
     */
    @PostMapping(value = "/bookmarks")
    public Dto<Map<String, Object>> insertBookmark(
//			@PathVariable String place_id, 
//			@RequestParam(required = false) String user_id,
//			@RequestParam(defaultValue = "place") String gubun		// place, youtube, naver, tistory, app
            @RequestParam Map<String, String> allRequestParams) {
        Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();
        Map<String, Object> return_map = Maps.newHashMap();

        List<String> gubun_list = Lists.newArrayList("place", "naver", "youtube", "tistory", "app");

        try {
            if (!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id") == null || allRequestParams.get("user_id").isBlank()) {
                return_dto.setCode(400);
                return_dto.setMessage("user_id는 필수 파라미터입니다. user_id를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()) {
                return_dto.setCode(400);
                return_dto.setMessage("place_id는 필수 파라미터입니다. place_id를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (!allRequestParams.containsKey("gubun") || allRequestParams.get("gubun") == null || allRequestParams.get("gubun").isBlank()) {    // place, youtube, naver, tistory, app
                return_dto.setCode(400);
                return_dto.setMessage("gubun은 필수 파라미터입니다. gubun 파라미터를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (!gubun_list.contains(allRequestParams.get("gubun"))) {        //gubun 값이 gubun_list 안에 없을 경우
                return_dto.setCode(400);
                return_dto.setMessage("gubun 파라미터를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (allRequestParams.get("gubun").equals("place")) {                // gubun 값이 없거나 default인 경우
                if (!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()) {
                    return_dto.setCode(400);
                    return_dto.setMessage("gubun 값이 없거나 place일 경우 place_id 파라미터는 필수입니다. 확인해주세요.");
                    return_map.put("result_message", "parameter failed");
                    return_dto.setDataList(return_map);
                    return return_dto;
                }
            } else {
                if (!allRequestParams.containsKey("id") || allRequestParams.get("id") == null || allRequestParams.get("id").isBlank()) {
                    return_dto.setCode(400);
                    return_dto.setMessage("gubun 값이 youtube, naver, blog, app일 경우 id 파라미터는 필수입니다. 확인해주세요.");
                    return_map.put("result_message", "parameter failed");
                    return_dto.setDataList(return_map);
                    return return_dto;
                }
            }

            return_map = common.insertBookmarks(allRequestParams);
            return_dto.setDataList(return_map);

        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_map.put("result_message", e.getMessage());
            return_dto.setDataList(return_map);
            return return_dto;
        }


        return return_dto;
    }

    /**
     * 북마크 삭제
     *
     * @return
     */
    @DeleteMapping(value = "/bookmarks")
    public Dto<Map<String, Object>> deleteBookmark(
            @RequestParam Map<String, String> allRequestParams) {
        Dto<Map<String, Object>> return_dto = new Dto<Map<String, Object>>();
        Map<String, Object> return_map = Maps.newHashMap();

        List<String> gubun_list = Lists.newArrayList("place", "naver", "youtube", "tistory", "app");

        try {
            if (!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id") == null || allRequestParams.get("user_id").isBlank()) {
                return_dto.setCode(400);
                return_dto.setMessage("user_id는 필수 파라미터입니다. user_id를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()) {
                return_dto.setCode(400);
                return_dto.setMessage("place_id는 필수 파라미터입니다. place_id를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (!allRequestParams.containsKey("gubun") || allRequestParams.get("gubun") == null || allRequestParams.get("gubun").isBlank()) {    // place, youtube, naver, tistory, app
                return_dto.setCode(400);
                return_dto.setMessage("gubun은 필수 파라미터입니다. gubun 파라미터를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (!gubun_list.contains(allRequestParams.get("gubun"))) {        //gubun 값이 gubun_list 안에 없을 경우
                return_dto.setCode(400);
                return_dto.setMessage("gubun 파라미터를 확인해주세요.");
                return_map.put("result_message", "parameter failed");
                return_dto.setDataList(return_map);
                return return_dto;
            }
            if (allRequestParams.get("gubun").equals("place")) {                // gubun 값이 없거나 default인 경우
                if (!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()) {
                    return_dto.setCode(400);
                    return_dto.setMessage("gubun 값이 없거나 place일 경우 place_id 파라미터는 필수입니다. 확인해주세요.");
                    return_map.put("result_message", "parameter failed");
                    return_dto.setDataList(return_map);
                    return return_dto;
                }
            } else {
                if (!allRequestParams.containsKey("id") || allRequestParams.get("id") == null || allRequestParams.get("id").isBlank()) {
                    return_dto.setCode(400);
                    return_dto.setMessage("gubun 값이 youtube, naver, blog, app일 경우 id 파라미터는 필수입니다. 확인해주세요.");
                    return_map.put("result_message", "parameter failed");
                    return_dto.setDataList(return_map);
                    return return_dto;
                }
            }

            return_map = common.deleteBookmark(allRequestParams);
            return_dto.setDataList(return_map);

        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_map.put("result_message", e.getMessage());
            return_dto.setDataList(return_map);
            return return_dto;
        }


        return return_dto;
    }

    @GetMapping("/bookmarkflag")
    public Dto<Map<String, Object>> selectBookmarkFlag(@RequestParam Map<String, String> allRequestParams){
        Dto<Map<String, Object>> return_dto = new Dto<>();
        Map<String, Object> return_map = Maps.newHashMap();

        try {
            if(!allRequestParams.containsKey("review_id") || allRequestParams.get("review_id").isBlank())
                throw new Exception("review_id 파라미터는 필수입니다. 해당 파라미터를 확인해주세요.");
            if(!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id").isBlank())
                throw new Exception("user_id 파라미터는 필수입니다. 해당 파라미터를 확인해주세요.");
            if(!allRequestParams.containsKey("sns_division") || allRequestParams.get("sns_division").isBlank())
                throw new Exception("sns_division 파라미터는 필수입니다. 해당 파라미터를 확인해주세요.");

            return_map = common.selectBookmarkFlag(allRequestParams);
            return_dto.setDataList(return_map);

        } catch (Exception e) {
            var error = Maps.newHashMap(new HashMap<String, Object>());
            error.put("error_message", e.getMessage());

            e.printStackTrace();
            return_dto.setCode(500);
            return_dto.setMessage("server error");
            return_map.put("result_message", e.getMessage());
            return_dto.setDataList(return_map);
            return return_dto;
        }

        return return_dto;
    }

}
