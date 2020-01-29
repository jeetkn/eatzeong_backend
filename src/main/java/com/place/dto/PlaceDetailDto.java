package com.place.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class PlaceDetailDto{
	private String place_id;
	private String place_name;
	private String place_address;
	private String road_place_address;
	private String tel_no;
	private String open_hours;
	private String buisness_day;
	private String shutdown_flag;
	private String google_place_id;
	private String kakao_place_id;
	private String naver_place_id;
	private String latitude;
	private String longitude;
	private String category_name;
	private String category_detail;
	private String google_place_name;
	private double rating;
	private String thumbnail;
}
