package com.place.dto;

import lombok.Data;

@Data
public class Dto<T>{
	private int code;
	private String message;
	private T dataList;
	
	public Dto() {
		this.message = "호출 성공!!";
		this.code = 200;
	}
}
