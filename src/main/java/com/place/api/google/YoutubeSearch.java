package com.place.api.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.json.simple.JSONObject;

import com.place.api.CommonApi;

public class YoutubeSearch extends CommonApi{

	URL url;
	
	public YoutubeSearch() {
		this.setDomain("https://content.googleapis.com/youtube/v3/search?");
//		this.setKey("AIzaSyB25Kz59gEEBTq_H-PLkuBfTHnLfMlAFq8");
		this.setKey("AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc");
	}
	
	@Override
	public void checkFields(Map<String, String> fields) throws Exception {
		if(!fields.containsKey("q"))
			throw new Exception("q 필드는 필수입니다. 필드를 확인해주세요.");
		if(!fields.containsKey("part"))
			throw new Exception("part 필드는 필수입니다. 필드를 확인해주세요.");
	}

	@Override
	public String callApi(String URL) throws Exception {
		url = new URL(URL);
		
		System.out.println("CALL YOUTUBE SEARCH API : " + url);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
		
		return br.readLine();
	}

}
