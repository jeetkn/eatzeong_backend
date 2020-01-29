package com.place.dto;

import lombok.Data;

@Data
public class PortalReviewDto {
	private String review_id;
	private String place_id;
	private int place_seq;
	private String author;
	private String write_time;
	private String write_date;
	private String portal;
	private String g_content;
	private int g_rating;
	private String y_description;
	private String y_title;
	private String y_video_id;
	private String y_thumbnail_url;
}
