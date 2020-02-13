package com.place.dto;

import lombok.Data;

@Data
public class PortalBlogDto {
	private String review_id;
	private String place_id;
	private String author;
	private String portal;
	private String title;
	private String description;
	private String write_date;
	private String write_time;
	private String url;
	private String thumbnail_url;
}
