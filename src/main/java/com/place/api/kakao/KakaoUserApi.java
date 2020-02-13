package com.place.api.kakao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.place.dto.PlaceDto;

public class KakaoUserApi {
	//properties 에 작성 한 restfulApiKey 세팅
	private String restfulKey = "b4bd7e75365a705323622c57d0b7e406";


	//기본적인 정보를 바탕 (access_token등) 으로 유저에 대한 상세 정보 조회
	public JSONObject kakaoUserDetailData(String access_token) {
		String requestUrl = "https://kapi.kakao.com/v2/user/me"; //호출 할 HOST
        String parameters = ""; //get으로 넘길파라메터 세팅 초기값
        
        JSONObject jsonObj = null;
        try {
        	JSONParser parser = new JSONParser();
        	Object obj = new Object();
			 requestUrl += parameters;

	    	   String httpMethod = "POST"; //호출방식 세팅
	    	   HttpsURLConnection conn;
	    	   BufferedReader reader = null;
	    	   InputStreamReader isr = null;
	    	   
	    	   final URL url = new URL(requestUrl);
    		   conn = (HttpsURLConnection) url.openConnection();
    		   conn.setRequestMethod(httpMethod); //호출 메소드 타입
    		   conn.setRequestProperty("Authorization", "Bearer " + access_token);
    		   conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    		   conn.setRequestProperty("charset", "utf-8");

    		   final int responseCode = conn.getResponseCode();
    		   
    		   System.out.println(String.format("\nSending '%s' request to URL : %s", httpMethod, requestUrl));
    		   System.out.println("Response Code : " + responseCode);
    		   if (responseCode == 200) {
				    isr = new InputStreamReader(conn.getInputStream());
    		   }else {
    		   	    isr = new InputStreamReader(conn.getErrorStream());
    		   }
    		   
    		   reader = new BufferedReader(isr);
    		   final StringBuffer buffer = new StringBuffer();
    		   String line;
    		   while ((line = reader.readLine()) != null) {
				    buffer.append(line);
    		   }

			   parser = new JSONParser();
			   obj = parser.parse(buffer.toString());
			   jsonObj = (JSONObject) obj;
			   
		} catch (UnsupportedEncodingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	//우리 앱과 유저 카카오 연결 해제 (탈퇴개념)
	public void kakaoUserDisconnect(String target_id) {
		String requestUrl = "https://kapi.kakao.com/v1/user/unlink"; //호출 할 HOST
        String parameters = ""; //get으로 넘길파라메터 세팅 초기값
        
        JSONObject jsonObj = null;
        try {
        	JSONParser parser = new JSONParser();
        	Object obj = new Object();
			parameters += "?target_id_type=user_id&target_id=" + target_id;
			 requestUrl += parameters;

	    	   String httpMethod = "POST"; //호출방식 세팅
	    	   HttpsURLConnection conn;
	    	   BufferedReader reader = null;
	    	   InputStreamReader isr = null;
	    	   
	    	   final URL url = new URL(requestUrl);
    		   conn = (HttpsURLConnection) url.openConnection();
    		   conn.setRequestMethod(httpMethod); //호출 메소드 타입
    		   conn.setRequestProperty("Authorization", "KakaoAK " + "272e65942ea9be4dcfa4902f93d62caf");
    		   conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    		   conn.setRequestProperty("charset", "utf-8");

    		   final int responseCode = conn.getResponseCode();
    		   
    		   System.out.println(String.format("\nSending '%s' request to URL : %s", httpMethod, requestUrl));
    		   System.out.println("Response Code : " + responseCode);
    		   if (responseCode == 200) {
				    isr = new InputStreamReader(conn.getInputStream());
    		   }else {
    		   	    isr = new InputStreamReader(conn.getErrorStream());
    		   }
    		   
    		   reader = new BufferedReader(isr);
    		   final StringBuffer buffer = new StringBuffer();
    		   String line;
    		   while ((line = reader.readLine()) != null) {
				    buffer.append(line);
    		   }
				System.out.println(buffer.toString());

			   parser = new JSONParser();
			   obj = parser.parse(buffer.toString());
			   jsonObj = (JSONObject) obj;
			   System.out.println("document data"+jsonObj);
			   System.out.println("json 파싱 데이터 : " + jsonObj.toString());
		} catch (UnsupportedEncodingException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

    public JSONArray kakaoTest() {
		Boolean placeYN = false;
		ObjectMapper mapper = new ObjectMapper();
		JSONObject jsonObj = null;
		String jsonString = "";
		JSONArray array= null;
		List<PlaceDto> placeList = new ArrayList<PlaceDto>();
		try {
			JSONParser parser = new JSONParser();
		    
			//장소 존재하면 카카오 맵 정보api 미 호출
		        String requestUrl = "https://kauth.kakao.com/oauth/authorize"; //호출 할 HOST
		        String parameters = "";
		        int page = 1; //3페이지 까지 가지고 오기위한 수 
		        boolean request_check = true;
			        List<PlaceDto> apiRequestList = new ArrayList<PlaceDto>();
			        try {
							parameters = "?client_id=" + URLEncoder.encode(restfulKey, "UTF-8") + "&redirect_uri=http://localhost:8080&response_type=code";
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
			    	   requestUrl += parameters;
	
			    	   String httpMethod = "GET"; //호출방식 세팅
			    	   HttpsURLConnection conn;
			    	   OutputStreamWriter writer = null;
			    	   BufferedReader reader = null;
			    	   InputStreamReader isr = null;
			        
			    	   try {
			    		   final URL url = new URL(requestUrl);
			    		   conn = (HttpsURLConnection) url.openConnection();
			    		   conn.setRequestMethod(httpMethod); //호출 메소드 타입
			    		   //conn.setRequestProperty("Authorization", "KakaoAK " + restfulKey);
			    		   conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			    		   conn.setRequestProperty("charset", "utf-8");
	
			    		   final int responseCode = conn.getResponseCode();
			    		   
			    		   System.out.println(String.format("\nSending '%s' request to URL : %s", httpMethod, requestUrl));
			    		   System.out.println("Response Code : " + responseCode);
			    	
			    		   if (responseCode == 200) {
							    isr = new InputStreamReader(conn.getInputStream());
								   
			    		   }else {
			    		   	    isr = new InputStreamReader(conn.getErrorStream());
			    		   	    request_check = false;
			    		   	    
			    		   }
			    		   if(request_check) {
				    		   reader = new BufferedReader(isr);
				    		   final StringBuffer buffer = new StringBuffer();
				    		   String line;
				    		   while ((line = reader.readLine()) != null) {
								    buffer.append(line);
				    		   }
				    		  // parser = new JSONParser();
							   //obj = parser.parse(buffer.toString());
							   //jsonObj = (JSONObject) obj;
							   System.out.println(buffer.toString());
			    		   }
						  
						   
			    		   
						  /* for(int i = 0; i < placeArray.size(); i++) {
							   JSONObject placeObject = (JSONObject) placeArray.get(i);
							   PlaceVO p_VO = new PlaceVO();
							   
							   String category_division[] = ((String)placeObject.get("category_name")).split(" > ");
							   switch(category_division[1]) {
								   case "카페":
									   p_VO.setCategory("CE7");
									   break;
								   case "한식":
									   p_VO.setCategory("CT1");
									   break;
								   case "중식":
									   p_VO.setCategory("CT2");
									   break;
								   case "양식":
									   p_VO.setCategory("CT3");
									   break;
								   case "일식":
									   p_VO.setCategory("CT4");
									   break;
								   case "뷔페":
									   p_VO.setCategory("CT5");
									   break;
								   case "술집":
									   p_VO.setCategory("CT6");
									   break;
								   case "분식":
									   p_VO.setCategory("CT7");
									   break;
								   default:
									   p_VO.setCategory("CT8");
									   break;
							   }
							   
							   p_VO.setParent_category("FD6");
							   p_VO.setPlace_id((String)placeObject.get("id") + "K");//장소 id 
							   p_VO.setKakao_place_id((String)placeObject.get("id")); //카카오 장소 id
							   p_VO.setPlace_name((String)placeObject.get("place_name"));//장소 이름 
							   p_VO.setCategory_detail((String)placeObject.get("category_name"));//카테고리 이름  
							   p_VO.setPlace_address((String)placeObject.get("road_address_name"));//주소
							   p_VO.setTel_no((String)placeObject.get("phone"));//전화번호 
							   p_VO.setAdd_date(DayCheck.dayCheck());
							   p_VO.setAdd_time(DayCheck.dayCheck());
							   p_VO.setUpdate_date(DayCheck.dayCheck());
							   p_VO.setUpdate_time(DayCheck.dayCheck());
							   p_VO.setLatitude((String)placeObject.get("y"));//위도 
							   p_VO.setLongitude((String)placeObject.get("x"));//경도 
							   if((boolean)lastPageCheck.get("is_end")) { //현재 페이지가 마지막 페이지인지 여부
								   request_check = false;
							   }
							   apiRequestList.add(p_VO);
						   }*/
						   try {
							//placeService.placeInsert(apiRequestList);
						} catch (Exception e) {
							TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
							e.printStackTrace();
						}
						   jsonString = mapper.writeValueAsString(placeList);
						    
						  // obj = parser.parse(jsonString);
						   //array = (JSONArray) obj;
			        } catch (IOException e) {
			            e.printStackTrace();
			        }finally {
			            if (writer != null) try { writer.close(); } catch (Exception ignore) { }
			            if (reader != null) try { reader.close(); } catch (Exception ignore) { }
			            if (isr != null) try { isr.close(); } catch (Exception ignore) { }
			        }
		} catch (Exception e2) {
			e2.printStackTrace();
			e2.getMessage();
		}finally {
			  
		}
		 return array;
    }
}
