package com.place.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.place.dto.AppReviewDto;
import com.place.repository.AppReviewRepository;
import com.place.repository.PlaceRepository;
import com.place.service.AppReviewService;
import com.place.service.interfaces.AppReviewServiceInterface;

@Service("com.place.service.AppReviewService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
public class AppReviewService implements AppReviewServiceInterface {
	private static final Logger logger = LoggerFactory.getLogger(AppReviewService.class);
//	private final String UPLOAD_DERECTORY = "/home/ubuntu/upload_image/";		// 개발서버
//	private final String HOST_URL = "http://api.matitzung.shop";				// 개발서버
	private final String UPLOAD_DERECTORY = "/Dev/Upload_file/review/";			// 로컬서버
	private final String HOST_URL = "http://localhost:8080";					// 로컬서버

	@Inject
	AppReviewRepository app_review;
	
	
	@Override
	public Map<String, Object> selectAppReview(AppReviewDto app_review_dto) throws Exception {
		List<AppReviewDto> review_dto = Lists.newArrayList();
		Map<String, Object> return_map = Maps.newHashMap();
		List<String> image_url_list = Lists.newArrayList();
		
		System.out.println(app_review_dto);
		review_dto = app_review.selectAppReview(app_review_dto);
		
		if(review_dto.size() > 0) {
			return_map.putIfAbsent("place_id", review_dto.get(0).getPlace_id());
			return_map.putIfAbsent("review_id", review_dto.get(0).getReview_id());
			return_map.putIfAbsent("reivew_contents", review_dto.get(0).getReview_contents());
			return_map.putIfAbsent("place_id", review_dto.get(0).getPlace_id());
			return_map.putIfAbsent("rating_point", review_dto.get(0).getRating_point());
			return_map.putIfAbsent("user_id", review_dto.get(0).getReview_user_id());
			return_map.putIfAbsent("image_url", fileImageUrl(review_dto).get(review_dto.get(0).getReview_id()));
		}else {
			throw new Exception("리뷰 조회 결과가 없습니다.");
		}
		
		return return_map;
	}
	
	@Override
	public List<Map<String, Object>> selectAppReviewList(String place_id) throws Exception {
		List<Map<String, Object>> return_dto_list = Lists.newArrayList();
		List<AppReviewDto> dto_list = Lists.newArrayList();
		AppReviewDto review_dto = new AppReviewDto();
		
		try {
			List<AppReviewDto> review_dto_list = Lists.newArrayList();
			
			review_dto.setPlace_id(place_id);
			review_dto_list = app_review.selectAppReviewList(review_dto);
			
			Multimap<String, String> review_multi_map= MultimapBuilder.hashKeys().arrayListValues().build();
			for(AppReviewDto dto : review_dto_list) {
				if(dto.getAttach_name() != null) {
					review_multi_map.put(dto.getReview_id(), HOST_URL + "/review/" + dto.getAttach_name());
				}
				if(dto_list.size() < 1) {
					dto_list.add(dto);
				}else {
					int max_index = dto_list.size() - 1;
					if(!dto_list.get(max_index).getReview_id().equals(dto.getReview_id())) {
						dto_list.add(dto);
					}
				}
			}
			
			for(AppReviewDto dto:dto_list) {
				Map<String, Object> dto_map = new TreeMap<String, Object>().descendingMap();
				dto_map.put("review_id", dto.getReview_id());
				dto_map.put("review_contents", dto.getReview_contents());
				dto_map.put("review_user_id", dto.getReview_user_id());
				dto_map.put("like_count", dto.getLike_count());
				dto_map.put("rating_point", dto.getRating_point());
				dto_map.put("image_url", (List<String>)review_multi_map.get(dto.getReview_id()));
				
				return_dto_list.add(dto_map);
			}
			
			if(return_dto_list.isEmpty()) {
				Map<String, Object> result_message = Maps.newHashMap();
				result_message.put("result_count", 0);
				result_message.put("result_message", "결과 없음");
				return_dto_list.add(result_message);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		return return_dto_list;
	}
	

	/**
	 *	리뷰 삽입
	 */
	@Override
	public void insertAppReview(AppReviewDto app_review_dto, ArrayList<MultipartFile> files) throws Exception {
		
		try {
			if(files!=null) {
				if(files.size() > 0)
					app_review_dto.setFile_flag(true);
			}
				
			int count = app_review.selectAppReviewCount(app_review_dto);
			if(count < 1) {
				app_review.insertAppReview(app_review_dto);
				app_review_dto = app_review.selectAppReview(app_review_dto).get(0);
				
				if(files.size() > 0)
					fileUpload(app_review_dto, files);
			}else {
				throw new Exception("리뷰가 이미 존재합니다.");
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 *	리뷰 수정
	 */
	@Override
	public void reviewUpdate(AppReviewDto app_review_dto, ArrayList<MultipartFile> files) throws Exception{

		try {
			
			int cnt = app_review.selectAppReviewCount(app_review_dto);
			if(cnt < 1) {
				throw new Exception("리뷰가 존재하지 않습니다.");
			}else if(cnt > 1){
				throw new Exception("리뷰가 여러개 존재합니다.");
			}else {
				if(files!=null) {
					if(files.size() > 0)
						app_review_dto.setFile_flag(true);
				}
				app_review.updateAppReview(app_review_dto);
				app_review_dto = app_review.selectAppReview(app_review_dto).get(0);
				if(files.size() > 0)
					fileUpload(app_review_dto, files);
			}
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	
	@Override
	public void deleteAppReview(AppReviewDto app_review_dto) throws Exception{
		try {
			int cnt = app_review.selectAppReviewCount(app_review_dto);
			if(cnt < 1) 
				throw new Exception("리뷰가 존재하지 않습니다.");
			else
				app_review.deleteAppReview(app_review_dto);
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * 
	 * 
	 * @param app_review_dto
	 * @param files
	 * @throws Exception
	 */
	private void fileUpload(AppReviewDto app_review_dto, List<MultipartFile> files) throws Exception{
		List<Map<String, String>> attachment_list = Lists.newArrayList();
		
		if(files!=null) {
			int index = 0;
			for(MultipartFile file : files) {
				Map<String, String> attachment_map = Maps.newHashMap();
				String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
				Random rand = new Random(System.currentTimeMillis());
				
				String file_name = today + "_" + Math.abs(rand.nextInt(999999)+100000) + "." + FilenameUtils.getExtension(file.getOriginalFilename());
				
				File file2 = new File(UPLOAD_DERECTORY, file_name);
				FileUtils.writeByteArrayToFile(file2, file.getBytes());
				
				attachment_map.put("attach_number", app_review_dto.getAttach_number());
				attachment_map.put("attach_name", file_name);
				attachment_map.put("attach_seq", String.valueOf(index++));
				attachment_map.put("attach_route", file.getName());
				attachment_map.put("attach_extension", file.getContentType());
				attachment_map.put("attach_size", String.valueOf(file.getSize()));
				attachment_map.put("review_user_id", app_review_dto.getReview_user_id());
				
				attachment_list.add(attachment_map);
			}
			
			if(attachment_list.size() > 0) {
				app_review.insertAppReviewAttachment(attachment_list);
			}
		}
	}
	
	
	/**
	 * 파일 이미지 URL 만드는 함수
	 * 
	 * @return List<String>
	 */
	private Multimap<String, String> fileImageUrl(List<AppReviewDto> review_dto_list) {
		List<AppReviewDto> dto_list = Lists.newArrayList();
		Multimap<String, String> review_multi_map= MultimapBuilder.hashKeys().arrayListValues().build();
		
		if(!(review_dto_list.get(0).getAttach_number() == null)) {
			for(AppReviewDto dto : review_dto_list) {
				if(dto.getAttach_name() != null) {
					review_multi_map.put(dto.getReview_id(), HOST_URL + "/review/" + dto.getAttach_name());
				}
				if(dto_list.size() < 1) {
					dto_list.add(dto);
				}else {
					int max_index = dto_list.size() - 1;
					if(!dto_list.get(max_index).getReview_id().equals(dto.getReview_id())) {
						dto_list.add(dto);
					}
				}
			}
			
			return review_multi_map;
		}else {
			return review_multi_map;
		}
	}

	

}
