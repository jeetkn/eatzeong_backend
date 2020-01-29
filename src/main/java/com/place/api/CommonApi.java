package com.place.api;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class CommonApi {
	private String domain;
	private String key;
	
	/**
	 * API 호출 인터페이스
	 * 
	 * @param apiName - 호출 api명
	 * @param fields - API 호출 파라미터, < 필드명 : 필드값 >
	 * @throws Exception
	 */
	public String CreateURL(Map<String, String> fields) throws Exception {
		
		String requestURL = getDomain();
		List<String> fieldList = new ArrayList<String>();
       
        checkFields(fields);
		
		Iterator<String> iterator = fields.keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = URLEncoder.encode(fields.get(key), "UTF-8");
			fieldList.add(key+"="+value);
		}
		requestURL += StringUtils.join(fieldList, "&");
		if(!key.isEmpty())
			requestURL += "&key="+getKey(); 
		
		return requestURL;
	}

	/**
	 * API호출 시 필요한 필수 필드 여부 확인
	 * 
	 * @param fields
	 * @throws Exception
	 */
	public abstract void checkFields(Map<String, String> fields) throws Exception;
	
	/**
	 * API 호출
	 * 
	 * @param URL
	 * @return	json문자열 반환
	 * @throws Exception
	 */
	public abstract String callApi(String URL) throws Exception;
	
}