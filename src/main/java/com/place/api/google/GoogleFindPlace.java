package com.place.api.google;

import java.net.URI;
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



public class GoogleFindPlace extends CommonApi{
	
	RestTemplate restTemplate = new RestTemplate(); 
	URI uri;
	
	public GoogleFindPlace() {
		this.setDomain("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?");
		this.setKey("AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc");
	}

	@Override
	public void checkFields(Map<String, String> fields) throws Exception {
		// TODO Auto-generated method stub
		if(!fields.containsKey("input"))
			throw new Exception("input는 필수입니다. 필드를 확인해주세요.");
		if(!fields.containsKey("inputtype"))
			throw new Exception("inputtype는 필수입니다. 필드를 확인해주세요.");	
	}

	@Override
	public String callApi(String URL) throws Exception {
		
		HttpHeaders headers = new HttpHeaders();
		JSONParser jsonparser = new JSONParser();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set("Authorization", this.getKey()); 
	    
	    HttpEntity entity = new HttpEntity("parameters", headers); 
		
	    uri = URI.create(URL);
	    
	    System.out.println("CALL GOOGLE_FIND_PLACE API : " + uri);
		ResponseEntity response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);		
		
		int status = response.getStatusCodeValue();
		if(!(status==200)) 
			throw new Exception("통신에 실패하였습니다. status : " + status);
		
		return response.getBody().toString();
	}


}
