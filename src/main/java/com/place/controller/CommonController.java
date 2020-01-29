package com.place.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;
import javax.inject.Inject;

import com.place.service.MyService;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.place.dto.Dto;
import com.place.service.CommonService;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/v1")
@CrossOrigin(origins = {"*"})
@SpringBootApplication(scanBasePackages = {"com.place.service"})
@Slf4j
public class CommonController {
	private static final Logger logger = LoggerFactory.getLogger(CommonController.class);
	AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
	static final String URL1 = "http://localhost:8090/service1?req={req}";
	static final String URL2 = "http://localhost:8090/service2?req={req}";
	WebClient client = WebClient.create();
	
	@Resource(name="com.place.service.CommonService")
	CommonService common;

	@Resource(name="com.place.service.MyService")
	MyService myService;

	@GetMapping("/rest")
	public Mono<String> rest(int idx) {
//        Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange();
//        Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
//        return body;
		return client.get().uri(URL1, idx).exchange()
				.flatMap(c -> c.bodyToMono(String.class))
				.doOnNext(c -> log.info(c.toString()))
				.flatMap(res1 -> client.get().uri(URL2, res1).exchange())
				.flatMap(c -> c.bodyToMono(String.class))
				.doOnNext(c -> log.info(c.toString()))
				.flatMap(res2 -> Mono.fromCompletionStage(myService.work(res2)))
				.doOnNext(c -> log.info(c.toString()));
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
	
	@GetMapping(value = "/autocomplete")
	public List<String> selectKeyword(@RequestParam("term") String keyword){
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
			@RequestParam Map<String, String> allRequestParams
			){
		
		Dto<List<Map<String, Object>>> return_dto = new Dto<List<Map<String, Object>>>();
		List<Map<String, Object>> return_dataList = Lists.newArrayList();
		Map<String, Object> return_map = Maps.newHashMap();
		
		List<String> gubun_list = Lists.newArrayList("place","naver","youtube","tistory","app");
		
		try {
			
			if(!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id") == null || allRequestParams.get("user_id").isBlank()) {
				return_dto.setCode(400);
				return_dto.setMessage("user_id는 필수 파라미터입니다. user_id를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dataList.add(return_map);
				return_dto.setDataList(return_dataList);
				return return_dto;
			}
			if (!allRequestParams.containsKey("gubun") || allRequestParams.get("gubun") == null || allRequestParams.get("gubun").isBlank()) {	// place, youtube, naver, tistory, app
				allRequestParams.putIfAbsent("gubun", "place");
			}
			if(!gubun_list.contains(allRequestParams.get("gubun"))) {		//gubun 값이 gubun_list 안에 없을 경우
				return_dto.setCode(400);
				return_dto.setMessage("gubun 파라미터를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dataList.add(return_map);
				return_dto.setDataList(return_dataList);
				return return_dto;
			}
			
			return_dto.setDataList(common.selectBookmarks(allRequestParams));
			
		}catch (Exception e) {
			var error = Maps.newHashMap(new HashMap<String, Object>());
			error.put("error_message", e.getMessage());
			
			e.printStackTrace();
			return_dto.setCode(500);
			return_dto.setMessage("서버 오류");
			return_dataList.add(error);
			return_dto.setDataList(return_dataList);
			return return_dto;
		}
		
		return return_dto;
	}
	
	
	/**
	 * 북마크 생성
	 * 
	 * @param place_id
	 * @param user_id
	 * @param gubun
	 * @return
	 */
	@PostMapping(value = "/bookmarks")
	public Dto<Map<String, Object>> insertBookmark(
//			@PathVariable String place_id, 
//			@RequestParam(required = false) String user_id,
//			@RequestParam(defaultValue = "place") String gubun		// place, youtube, naver, tistory, app
			@RequestParam Map<String, String> allRequestParams
			){
		Dto<Map<String, Object>> return_dto = new Dto<Map<String,Object>>();
		Map<String, Object> return_map = Maps.newHashMap();
		
		List<String> gubun_list = Lists.newArrayList("place","naver","youtube","tistory","app");
		
		try {
			if(!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id") == null || allRequestParams.get("user_id").isBlank()) {
				return_dto.setCode(400);
				return_dto.setMessage("user_id는 필수 파라미터입니다. user_id를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if(!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()) {
				return_dto.setCode(400);
				return_dto.setMessage("place_id는 필수 파라미터입니다. place_id를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if (!allRequestParams.containsKey("gubun") || allRequestParams.get("gubun") == null || allRequestParams.get("gubun").isBlank()) {	// place, youtube, naver, tistory, app
				return_dto.setCode(400);
				return_dto.setMessage("gubun은 필수 파라미터입니다. gubun 파라미터를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if(!gubun_list.contains(allRequestParams.get("gubun"))) {		//gubun 값이 gubun_list 안에 없을 경우
				return_dto.setCode(400);
				return_dto.setMessage("gubun 파라미터를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if(allRequestParams.get("gubun").equals("place")) {				// gubun 값이 없거나 default인 경우
				if(!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()){
					return_dto.setCode(400);
					return_dto.setMessage("gubun 값이 없거나 place일 경우 place_id 파라미터는 필수입니다. 확인해주세요.");
					return_map.put("result_message", "parameter failed");
					return_dto.setDataList(return_map);
					return return_dto;
				}
			}else {
				if(!allRequestParams.containsKey("id") || allRequestParams.get("id") == null || allRequestParams.get("id").isBlank()){
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
			return_dto.setMessage("서버 오류");
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
			@RequestParam Map<String, String> allRequestParams
			){
		Dto<Map<String, Object>> return_dto = new Dto<Map<String,Object>>();
		Map<String, Object> return_map = Maps.newHashMap();
		
		List<String> gubun_list = Lists.newArrayList("place","naver","youtube","tistory","app");
		
		try {
			if(!allRequestParams.containsKey("user_id") || allRequestParams.get("user_id") == null || allRequestParams.get("user_id").isBlank()) {
				return_dto.setCode(400);
				return_dto.setMessage("user_id는 필수 파라미터입니다. user_id를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if(!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()) {
				return_dto.setCode(400);
				return_dto.setMessage("place_id는 필수 파라미터입니다. place_id를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if (!allRequestParams.containsKey("gubun") || allRequestParams.get("gubun") == null || allRequestParams.get("gubun").isBlank()) {	// place, youtube, naver, tistory, app
				return_dto.setCode(400);
				return_dto.setMessage("gubun은 필수 파라미터입니다. gubun 파라미터를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if(!gubun_list.contains(allRequestParams.get("gubun"))) {		//gubun 값이 gubun_list 안에 없을 경우
				return_dto.setCode(400);
				return_dto.setMessage("gubun 파라미터를 확인해주세요.");
				return_map.put("result_message", "parameter failed");
				return_dto.setDataList(return_map);
				return return_dto;
			}
			if(allRequestParams.get("gubun").equals("place")) {				// gubun 값이 없거나 default인 경우
				if(!allRequestParams.containsKey("place_id") || allRequestParams.get("place_id") == null || allRequestParams.get("place_id").isBlank()){
					return_dto.setCode(400);
					return_dto.setMessage("gubun 값이 없거나 place일 경우 place_id 파라미터는 필수입니다. 확인해주세요.");
					return_map.put("result_message", "parameter failed");
					return_dto.setDataList(return_map);
					return return_dto;
				}
			}else {
				if(!allRequestParams.containsKey("id") || allRequestParams.get("id") == null || allRequestParams.get("id").isBlank()){
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
			return_dto.setMessage("서버 오류");
			return_map.put("result_message", e.getMessage());
			return_dto.setDataList(return_map);
			return return_dto;
		}
		
		
		return return_dto;
	}
	
}
