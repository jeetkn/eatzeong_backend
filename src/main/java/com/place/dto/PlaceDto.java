package com.place.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PlaceDto {
	private String place_name;
	private String place_id;
	private String tel_no;
	private String place_address;
	private String road_place_address;
	private String category;
	private String catogory_detail;
	private String latitude;
	private String longitude;
	private String open_hours;
	private String buisness_day;
	private int comment_count;
	private String google_rating;
	private String app_rating;
	private String keyword;
	private String shutdown_flag;
	private String google_place_id;
	private String kakao_place_id;
	private String naver_place_id;
	private String category_detail;
	private String parent_category;
	private String category_name;
	private String blog_thumbnail;
	private String app_thumbnail;
	private String filter_category;
	private String current_location;
	private String naver_blog_count;
	private String daum_blog_count;
	private String google_review_count;
	private String youtube_review_count;
	private String app_review_count;
	private String user_id;
	private String sns_division;
}

