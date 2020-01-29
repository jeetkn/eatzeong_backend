package com.place.service.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;

/* 
 * 
 * 맵퍼에 관한 인터페이스 정의 해 주는 곳임.
 * 각 메소드명은 맵퍼 id 와 동일하며 resultType 로 
 * 메소드의 타입을 정한다.
 * 
 * */
@Repository("com.place.service.mapper.PlaceMapper") //--> 맵퍼위치 세팅

public interface PlaceMapper {
	
	public List<PlaceDto> selectPlaceList(PlaceDto dto) throws Exception;
	
	public int selectPlaceCount(PlaceDto dto) throws Exception;
	
	public int insertPlace(PlaceDto dto) throws Exception;
	
	public List<PlaceDetailDto> selectPlaceDetail(PlaceDetailDto dto) throws Exception;
	
	public int updatePlace(PlaceDetailDto dto) throws Exception;
	
}
