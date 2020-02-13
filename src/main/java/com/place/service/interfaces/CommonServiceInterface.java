package com.place.service.interfaces;

import com.place.dto.PlaceDto;

import java.util.List;
import java.util.Map;

public interface CommonServiceInterface {
	
	public Map<String, Object> selectArea() throws Exception;
	
	public List<String> suggestKeyword(String keyword) throws Exception;

	public List<Map<String, Object>> selectBookmarks(Map<String, String> allRequestParams) throws Exception;

	public Map<String, Object> insertBookmarks(Map<String, String> allRequestParams) throws Exception;

	public Map<String, Object> deleteBookmark(Map<String, String> allRequestParams) throws Exception;

    public List<String> getCustomSearch(String query, String portal) throws Exception;

    List<Map<String, String>> selectFirstArea() throws Exception;

	List<Map<String, String>> selectSecondArea(String area) throws Exception;

    List<Object> selectSuggestArea() throws Exception;

	int selectMainPlacesCount(PlaceDto place_dto) throws Exception;

	List<Map<String, Object>> selectMainPlaces(PlaceDto place_dto) throws Exception;
}
