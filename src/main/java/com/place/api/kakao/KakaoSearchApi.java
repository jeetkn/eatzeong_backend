package com.place.api.kakao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.place.api.CommonApi;

public class KakaoSearchApi extends CommonApi {

	URL url;
	private String restfulKey = "b4bd7e75365a705323622c57d0b7e406";

	public KakaoSearchApi() {
		this.setDomain("https://dapi.kakao.com/v2/local/search/keyword.json?");
		this.setKey("");
	}
	
	
	@Override
	public void checkFields(Map<String, String> fields) throws Exception {
		// TODO Auto-generated method stub
		if(!true)
			throw new Exception("?가 없습니다.");
	}

	@Override
	public String callApi(String URL) throws Exception {
		url = new URL(URL);
		int page = 1;
		
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "KakaoAK " + restfulKey);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("charset", "utf-8");
		int response_status = con.getResponseCode();
		BufferedReader br;
		
		System.out.println("CALL KAKAO MAP SEARCH API : " + url);
		
		if(response_status==200) { // 정상 호출
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else {  // 에러 발생
			System.out.println("ERROR!!!");
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
		    response.append(inputLine);
		}
		br.close();
		
		return response.toString();
	}

}
