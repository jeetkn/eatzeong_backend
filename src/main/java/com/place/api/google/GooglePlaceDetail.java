package com.place.api.google;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.place.api.CommonApi;

public class GooglePlaceDetail extends CommonApi{

	RestTemplate restTemplate = new RestTemplate(); 
	URI uri;
	
	public GooglePlaceDetail() {
		this.setDomain("https://maps.googleapis.com/maps/api/place/details/json?");
		this.setKey("AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc");
	}

	@Override
	public void checkFields(Map<String, String> fields) throws Exception {
		if(!fields.containsKey("place_id"))
			throw new Exception("place_id가 없습니다.");
	}

	@Override
	public String callApi(String URL) throws Exception {

		HttpHeaders headers = new HttpHeaders();
		JSONParser jsonparser = new JSONParser();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set("Authorization", this.getKey()); 
	    
	    HttpEntity entity = new HttpEntity("parameters", headers); 
		
	    uri = URI.create(URL);
		
	    System.out.println("CALL GOOGLE_PLACE_DETAIL API : " + uri);
		ResponseEntity response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);		
		
		int status = response.getStatusCodeValue();
		if(!(status==200)) 
			throw new Exception("통신에 실패하였습니다. status : " + status);
		
		return response.getBody().toString();
	}

	
}
