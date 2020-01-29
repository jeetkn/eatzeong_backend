package com.place.repository;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.place.dto.PlaceDetailDto;
import com.place.dto.PlaceDto;
import com.place.service.mapper.PlaceMapper;

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
}
