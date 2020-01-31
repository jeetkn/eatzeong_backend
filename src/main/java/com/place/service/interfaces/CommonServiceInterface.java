package com.place.service.interfaces;

import java.util.List;
import java.util.Map;

public interface CommonServiceInterface {
	
	public Map<String, Object> selectArea() throws Exception;
	
	public List<String> suggestKeyword(String keyword) throws Exception;

	public List<Map<String, Object>> selectBookmarks(Map<String, String> allRequestParams) throws Exception;

	public Map<String, Object> insertBookmarks(Map<String, String> allRequestParams) throws Exception;

	public Map<String, Object> deleteBookmark(Map<String, String> allRequestParams) throws Exception;

    public List<String> getCustomSearch(String query, String portal);
}
