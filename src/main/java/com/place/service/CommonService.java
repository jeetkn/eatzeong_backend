package com.place.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SortedSetMultimap;
import com.place.repository.CommonRepository;
import com.place.service.CommonService;
import com.place.service.interfaces.CommonServiceInterface;

@Service("com.place.service.CommonService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
public class CommonService implements CommonServiceInterface{
	private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
//	private final String HOST_URL = "http://api.matitzung.shop";				// 개발서버
	private final String HOST_URL = "http://localhost:8080";					// 로컬서버
	
	@Inject
	CommonRepository common;

	@Override
	public Map<String, Object> selectArea() throws Exception {
		BiMap<String, String> bimap_column = HashBiMap.create();
		SortedSetMultimap<String, Comparable> multimap = MultimapBuilder.linkedHashKeys().treeSetValues().build();
		List<String> large_category = Lists.newArrayList();
		List<Object> medium_category = Lists.newArrayList();
		Map<String, Object> result_map = Maps.newLinkedHashMap();
	
		
		for(Map<String, String> map : common.selectArea()) {
			if(map.get("parent_area").equals("*")) {
				bimap_column.put(map.get("area_name"), map.get("area"));
				large_category.add(map.get("area_name"));
			}
			else {
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
	 *	북마크 조회
	 */
	public List<Map<String, Object>> selectBookmarks(Map<String, String> allRequestParams) throws Exception {
		List<Map<String, Object>> query_list = Lists.newArrayList();
		List<Map<String, Object>> result_list = Lists.newArrayList();
		logger.info("Parameters : " + allRequestParams.toString());
		
		try {
			query_list = common.selectBookmarks(allRequestParams);
			logger.debug(query_list.toString());
			
			if(query_list.size() < 1) {
				Map<String, Object> return_map = Maps.newHashMap();
				return_map.put("result_count", 0);
				return_map.put("result_message", "검색결과 없음");
				result_list.add(return_map);
				return result_list;
			}else {
				result_list = parseBookmarks(query_list, allRequestParams.get("gubun"));
				return result_list;
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}
	
	@Override
	public Map<String, Object> insertBookmarks(Map<String, String> allRequestParams) throws Exception {
		Map<String, Object> return_map = Maps.newHashMap(); 
		logger.info("Parameters : " + allRequestParams.toString());
		
		try {
			int count = common.selectBookmarkCnt(allRequestParams);
			logger.debug("북마크 갯수 : " + count);
			if(count < 1) {
				common.insertBookmarks(allRequestParams);
				return_map.put("result_message", "insert 성공");
			}else {
				return_map.put("result_message", "insert 실패. 해당 데이터의 북마크가 이미 존재합니다.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		return return_map;
	}
	
	@Override
	public Map<String, Object> deleteBookmark(Map<String, String> allRequestParams) throws Exception {
		Map<String, Object> return_map = Maps.newHashMap(); 
		logger.info("Parameters : " + allRequestParams.toString());
		
		try {
				common.deleteBookmarks(allRequestParams);
				return_map.put("result_message", "delete 성공");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		return return_map;
	}
	
	private List<Map<String, Object>> parseBookmarks(List<Map<String, Object>> query_list, String gubun) {
		List<Map<String, Object>> result_list = Lists.newArrayList();
		
		if(gubun.equals("youtube")) {
			for(Map<String, Object> map : query_list) {
				Map<String, Object> return_map = Maps.newHashMap();
				
				return_map.put("place_id", map.get("place_id"));
				return_map.put("gubun", map.get("gubun"));
				return_map.put("place_name", map.get("place_name"));
				return_map.put("review_id", map.get("review_id"));
				return_map.put("write_date", map.get("write_date").toString());
				return_map.put("title", map.get("y_title"));
				return_map.put("description", map.get("y_description"));
				return_map.put("y_video_id", map.get("y_video_id"));
				return_map.put("thumbnail_url", map.get("y_thumbnail_url"));
				
				result_list.add(return_map);
			}
		}else if(gubun.equals("naver") || gubun.equals("tistory")) {
			for(Map<String, Object> map : query_list) {
				Map<String, Object> return_map = Maps.newHashMap();
				
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
		}else if(gubun.equals("app")) {
			for(Map<String, Object> map : query_list) {
				Map<String, Object> return_map = Maps.newHashMap();
				
				return_map.put("place_id", map.get("place_id"));
				return_map.put("gubun", map.get("gubun"));
				return_map.put("place_name", map.get("place_name"));
				return_map.put("review_id", map.get("review_id"));
				return_map.put("write_date", map.get("add_date").toString());
				return_map.put("write_time", map.get("add_time"));
				return_map.put("review_contents", map.get("review_contents"));
				return_map.put("like_count", map.get("like_count"));
				return_map.put("rating_point", String.format("%d", map.get("rating_point")));
				return_map.put("write_user_id", map.get("review_user_id")); 
				
				result_list.add(return_map);
			}
		}else {
			for(Map<String, Object> map : query_list) {
				Map<String, Object> return_map = Maps.newHashMap();
				
				return_map.put("place_id", map.get("place_id"));
				return_map.put("gubun", map.get("gubun"));
				return_map.put("place_name", map.get("place_name"));
				if(!(map.get("open_hours") == null || map.get("open_hours") == ""))
					return_map.put("open_hours", map.get("open_hours"));
				else
					return_map.put("open_hours", "");
				
				if(!(map.get("rating_point") == null || map.get("rating_point") == ""))
					return_map.put("rating_point", String.format("%.1f", map.get("rating_point")));
				else
					return_map.put("rating_point", "");
				
				if(!(map.get("thumbnail") == null || map.get("thumbnail") == ""))
					return_map.put("thumbnail", HOST_URL + "/review/" + map.get("thumbnail"));
				else
					return_map.put("thumbnail", "");
				
				result_list.add(return_map);
			}
		}
		
		return result_list;
	}


	
}
