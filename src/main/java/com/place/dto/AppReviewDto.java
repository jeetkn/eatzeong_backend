package com.place.dto;

import java.util.List;

import lombok.Data;

@Data
public class AppReviewDto{
	private String review_contents;
	private String review_user_id;
	private String user_id;
	private String nickname;
	private String profile_image;
	private String sns_division;
	private String review_id;
	private String place_id;
	private String place_name;
	private String attach_number;
	private int like_count;
	private int like_flag;
	private float appreview_rating;
	private int rating_point;
	private String add_date;
	private String add_time;
	private String update_user_id;
	private boolean file_flag;
	private String attach_name;
	private List<String> image_url;
	private String category;
	private String category_name;
}
