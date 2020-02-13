package com.place.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service("com.place.service.ApiService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
@Slf4j
public class ApiService {

    @Value("${api.naver.clientID}") private String naver_client_id;
    @Value("${api.naver.clientSecret}") private String naver_client_secret;
    @Value("${api.kakao.key}") private String kakao_key;
    @Value("${api.google.key}") private String google_key;
    @Value("${api.google.customsearch.youtube_search_cx}") private String youtube_search_cx;
    @Value("${api.google.customsearch.naver_search_cx}") private String naver_search_cx;
    @Value("${api.google.customsearch.daum_search_cx}") private String daum_search_cx;

    public String naverSearchBlog(Map<String, String> field_map) throws Exception{

        String text = URLEncoder.encode(field_map.get("query"), "UTF-8");
        String apiURL = "https://openapi.naver.com/v1/search/blog?query="+ text + "&display=100";
        URL url = new URL(apiURL);
        log.info("CALL NAVER BLOG SEARCH : {}", url);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Naver-Client-Id", naver_client_id);
        con.setRequestProperty("X-Naver-Client-Secret", naver_client_secret);
        int responseCode = con.getResponseCode();
        BufferedReader br;
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
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


    public String daumSearchBlog(Map<String, String> field_map) throws Exception{

        String text = URLEncoder.encode(field_map.get("query"), "UTF-8");
        String apiURL = "https://dapi.kakao.com/v2/search/blog?query="+ text + "&size=50";
        URL url = new URL(apiURL);
        log.info("CALL DAUM BLOG SEARCH : {}", url);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "KakaoAK " + kakao_key);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        int responseCode = con.getResponseCode();
        BufferedReader br;
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
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

    public String youtubeSearchReviews(Map<String, String> field_map) throws Exception{
        String text = URLEncoder.encode(field_map.get("query"), "UTF-8");
        String apiURL = "https://dapi.kakao.com/v2/search/vclip?query="+ text + "&size=30";
        URL url = new URL(apiURL);
        log.info("CALL YOUTUBE REVIEW SEARCH : {}", url);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "KakaoAK " + kakao_key);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        int responseCode = con.getResponseCode();
        BufferedReader br;
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
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

    public String GoogleSearchReviews(Map<String, String> fields_map) throws Exception{
        String apiURL = "https://maps.googleapis.com/maps/api/place/details/json?place_id="
                + fields_map.get("google_place_id")
                + "&language=ko&fields=name,rating,formatted_phone_number,review,opening_hours&key="
                + google_key;
        URL url = new URL(apiURL);
        log.info("CALL GOOGLE REVIEW SEARCH : {}", url);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        BufferedReader br;
        if(responseCode==200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
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
