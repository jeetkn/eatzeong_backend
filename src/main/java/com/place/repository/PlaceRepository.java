package com.place.repository;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.service.mapper.PlaceMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class PlaceRepository {

	@Resource(name="com.place.service.mapper.PlaceMapper")
	PlaceMapper mapper;
	
	public List<PlaceDto> selectPlaceList(PlaceDto dto) throws Exception{
		return mapper.selectPlaceList(dto);
	}
	
	public void insertPlace(List<PlaceDto> dto_list) throws Exception {
		for(PlaceDto dto : dto_list) {
			int place_cnt = mapper.selectPlaceCount(dto);
			if(place_cnt <= 0) {
				mapper.insertPlace(dto);
			}
		}
	}

	public PlaceDetailDto selectPlaceDetail(PlaceDetailDto dto) throws Exception {
		List<PlaceDetailDto> dto_list = mapper.selectPlaceDetail(dto);
		if(dto_list.size() > 1) {
			for(PlaceDetailDto dto_tmp : dto_list) {
				if(!(dto_tmp.getGoogle_place_id() == null || dto_tmp.getGoogle_place_id().isBlank()))
					mapper.updatePlace(dto);
			}
		}
		return mapper.selectPlaceDetail(dto).get(0); //이부분 이해안감 조회는 리스트로 하는데 반환을 하나로함?
	}

	public void updatePlace(PlaceDetailDto dto) throws Exception{
		mapper.updatePlace(dto);
	}

	public void insertKeyword(PlaceDto dto) throws Exception{
		mapper.insertKeyword(dto);
	}

	public List<Map<String, Object>> getPopularSearches() throws Exception{
		return mapper.getPopularSearches();
	}

    public void insertNaverBlogs(List<Map<String, String>> result_list, Map<String, String> fields_map) throws Exception{
		for(Map<String, String> temp_map : result_list){
			temp_map.put("place_id", fields_map.get("place_id"));
			int count = mapper.selectNaverBlogCount(temp_map);
			if(count < 1)
				mapper.insertNaverBlog(temp_map);
		}
    }

	public int selectNaverBlogsCount(Map<String, String> fields_map) throws Exception{
		return mapper.selectNaverBlogsCount(fields_map);
	}

	public int selectDaumBlogsCount(Map<String, String> fields_map) throws Exception{
		return mapper.selectDaumBlogsCount(fields_map);
	}

	public List<Map<String, Object>> selectNaverBlogs(Map<String, String> fields_map) throws Exception{
		return mapper.selectNaverBlogs(fields_map);
	}


	public void insertDaumBlogs(List<Map<String, String>> daum_blog_list, Map<String, String> fields_map) throws Exception{
		for(Map<String, String> temp_map : daum_blog_list){
			temp_map.put("place_id", fields_map.get("place_id"));
			int count = mapper.selectDaumBlogCount(temp_map);
			if(count < 1)
				mapper.insertDaumBlog(temp_map);
		}
	}

	public List<Map<String, Object>> selectDaumBlogs(Map<String, String> fields_map) throws Exception {
		return mapper.selectDaumBlogs(fields_map);
	}

	public int selectYoutubeReviewsCount(Map<String, String> fields_map) throws Exception {
		return mapper.selectYoutubeReviewsCount(fields_map);
	}

    public void insertYoutubeReviews(List<Map<String, String>> youtube_review_list, Map<String, String> fields_map) throws Exception {
		for(Map<String, String> temp_map : youtube_review_list){
			temp_map.put("place_id", fields_map.get("place_id"));
			int count = mapper.selectYoutubeReviewCount(temp_map);
			if(count < 1)
				mapper.insertYoutubeReview(temp_map);
		}
    }

	public List<Map<String, Object>> selectYoutubeReviews(Map<String, String> fields_map) throws Exception {
		return mapper.selectYoutubeReviews(fields_map);
	}

	public int selectGoogleReviewsCount(Map<String, String> fields_map) throws Exception{
		return mapper.selectGoogleReviewsCount(fields_map);
	}

	public void insertGoogleReviews(List<Map<String, String>> google_review_list, Map<String, String> fields_map) throws Exception{
		for(Map<String, String> temp_map : google_review_list){
			temp_map.put("place_id", fields_map.get("place_id"));
			int count = mapper.selectGoogleReviewCount(temp_map);
			if(count < 1)
				mapper.insertGoogleReview(temp_map);
		}
	}

	public List<Map<String, Object>> selectGoogleReviews(Map<String, String> fields_map) throws Exception{
		return mapper.selectGoogleReviews(fields_map);
	}

    public int selectPlaceBookmark(PlaceDetailDto detail_dto) throws Exception{
		return mapper.selectPlaceBookmark(detail_dto);
    }

	public int selectPlaceAppReviewFlag(PlaceDetailDto detail_dto) throws Exception{
		return mapper.selectPlaceAppReviewFlag(detail_dto);
	}

    public List<Map<String, Object>> selectEatzeongReviews(Map<String, String> request_param) throws Exception{
		return mapper.selectEatzeongReviews(request_param);
    }

	public List<Map<String, Object>> selectEatzeongReviewAttachments(Map<String, Object> result_map) throws Exception{
		return mapper.selectEatzeongReviewAttachments(result_map);
	}

	public void insertBlacklist(Map<String, String> request_param) throws Exception{
		mapper.insertBlacklist(request_param);
	}

	public int selectBlacklistCount(Map<String, String> request_param) throws Exception{
		return mapper.selectBlacklistCount(request_param);
	}

	public void deleteBlacklist(Map<String, String> request_param) throws Exception{
		mapper.deleteBlacklist(request_param);
	}

	public void deleteBlacklistOne(Map<String, String> request_param) throws Exception{
		mapper.deleteBlacklistOne(request_param);
	}

    public Map<String, Object> selectBlacklistFlag(Map<String, String> request_param) throws Exception{
		return mapper.selectBlacklistFlag(request_param);
    }

	public List<Map<String, Object>> selectBlacklistAuthor(Map<String, String> request_param) throws Exception{
		return mapper.selectBlacklistAuthor(request_param);
	}

	public List<Map<String, Object>> selectBlacklistPost(Map<String, String> request_param) throws Exception{
		return mapper.selectBlacklistPost(request_param);
	}
}
