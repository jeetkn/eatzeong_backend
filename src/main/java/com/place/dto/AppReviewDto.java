package com.place.dto;

import java.util.List;

import lombok.Data;

@Data
public class AppReviewDto{
	private String review_contents;
	private String review_user_id;
	private String review_id;
	private String place_id;
	private String attach_number;
	private int like_count;
	private int rating_point;
	private String write_date;
	private String write_time;
	private String update_user_id;
	private boolean file_flag;
	private String attach_name;
	private List<String> image_url; 
}
