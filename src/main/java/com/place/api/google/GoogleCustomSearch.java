package com.place.api.google;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.place.api.CommonApi;

public class GoogleCustomSearch extends CommonApi{
	
	String naver_search_cx = "007124061159672905157:4dldrdpppep";			
	String daum_search_cx = "007124061159672905157:eoo651giqr9";
	String youtube_search_cx = "007124061159672905157:ceyxyhcsbvt";
	Map<String, String> search_cx = new HashMap<String, String>();
	
	
	public GoogleCustomSearch() {
		this.setDomain("https://www.googleapis.com/customsearch/v1/siterestrict?");
		this.setKey("AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc");
	}

	@Override
	public void checkFields(Map<String, String> fields) throws Exception {
		if(!fields.containsKey("q"))
			throw new Exception("q 필드는 필수입니다. 필드를 확인해주세요.");
	}

	@Override
	public String callApi(String URL) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		JSONParser jsonparser = new JSONParser();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set("Authorization", this.getKey()); 
	    
	    HttpEntity entity = new HttpEntity("parameters", headers); 
		
	    search_cx.put("NAVER", naver_search_cx);
	    search_cx.put("DAUM", daum_search_cx);
	    search_cx.put("YOUTUBE", youtube_search_cx);
	    
	    JSONObject response_json = new JSONObject();
	    
	    for(String search_cx_key : search_cx.keySet()) {
	    	URL url = new URL(URL+"&cx="+search_cx.get(search_cx_key));
	    	System.out.println("CALL " + search_cx_key + " CUSTOM SEARCH API : " + url);
			response_json.put(search_cx_key, jsonparser.parse(new InputStreamReader(url.openStream(), "UTF-8")));
	    }
	    
	    return response_json.toJSONString();
	}
	
	/**
	 * portal을 지정하여 API를 호출하는 함수
	 * 
	 * @param URL
	 * @param portal(NAVER, DAUM, YOUTUBE)
	 * @return
	 * @throws Exception
	 */
	public String callApi(String URL, String portal) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		JSONParser jsonparser = new JSONParser();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set("Authorization", this.getKey()); 
	    
	    HttpEntity entity = new HttpEntity("parameters", headers); 
		
	    search_cx.put("NAVER", naver_search_cx);
	    search_cx.put("DAUM", daum_search_cx);
	    search_cx.put("YOUTUBE", youtube_search_cx);
	    
	    JSONObject response_json = new JSONObject();
	    
	    for(String search_cx_key : search_cx.keySet()) {
	    	if(portal.toUpperCase() == search_cx_key.toUpperCase()) {
		    	URL url = new URL(URL+"&cx="+search_cx.get(search_cx_key));
		    	System.out.println("CALL " + search_cx_key + " CUSTOM SEARCH API : " + url);
				response_json.put(search_cx_key, jsonparser.parse(new InputStreamReader(url.openStream(), "UTF-8")));
	    	}
	    }
	    
	    return response_json.toJSONString();
	}
}
