package com.place.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.place.dto.AppReviewDto;
import com.place.repository.AppReviewRepository;
import com.place.service.interfaces.AppReviewServiceInterface;

@Service("com.place.service.AppReviewService")
@Transactional
@SpringBootApplication(scanBasePackages = {"com.place.repository"})
@Slf4j
public class AppReviewService implements AppReviewServiceInterface {

	@Value("${image.review}")
	private String IMAGE_DIRECTORY;

	@Value("${host.url}")
	private String HOST_URL;

	@Inject
	AppReviewRepository app_review;
	
	
	@Override
	public Map<String, Object> selectAppReview(AppReviewDto app_review_dto) throws Exception {
		List<AppReviewDto> review_dto = Lists.newArrayList();
		Map<String, Object> return_map = new LinkedHashMap<>();
		List<String> image_url_list = Lists.newArrayList();
		
		System.out.println(app_review_dto);
		review_dto = app_review.selectAppReview(app_review_dto);

		if(review_dto.size() > 0) {
			return_map.putIfAbsent("place_id", review_dto.get(0).getPlace_id());
			return_map.putIfAbsent("review_id", review_dto.get(0).getReview_id());
			return_map.putIfAbsent("review_user_id", review_dto.get(0).getReview_user_id());
			return_map.putIfAbsent("sns_division", review_dto.get(0).getSns_division());
			return_map.putIfAbsent("review_user_nickname", review_dto.get(0).getNickname());
			return_map.putIfAbsent("profile_image_url", HOST_URL + "/profile/" + review_dto.get(0).getProfile_image());
			return_map.putIfAbsent("like_count", review_dto.get(0).getLike_count());
			if(review_dto.get(0).getLike_flag() > 0)
				return_map.put("like_flag", true);
			else
				return_map.put("like_flag", false);
			return_map.putIfAbsent("reivew_contents", review_dto.get(0).getReview_contents());
			return_map.putIfAbsent("rating_point", review_dto.get(0).getRating_point());
			return_map.putIfAbsent("image_url", fileImageUrl(review_dto).get(review_dto.get(0).getReview_id()));
			return_map.putIfAbsent("write_date", review_dto.get(0).getAdd_date());
		}else {
			throw new Exception("리뷰 조회 결과가 없습니다.");
		}
		
		return return_map;
	}
	
	@Override
	public List<Map<String, Object>> selectAppReviewList(AppReviewDto review_dto) throws Exception {
		List<Map<String, Object>> return_dto_list = Lists.newArrayList();
		List<AppReviewDto> dto_list = Lists.newArrayList();

		try {
			List<AppReviewDto> review_dto_list = Lists.newArrayList();
			
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
				Map<String, Object> dto_map = new LinkedHashMap<String, Object>();

				dto_map.put("review_id", dto.getReview_id());
				dto_map.put("review_user_id", dto.getReview_user_id());
				dto_map.put("sns_division", dto.getSns_division());
				dto_map.put("review_user_nickname", dto.getNickname());
				dto_map.put("profile_image_url", HOST_URL + "/profile/" + dto.getProfile_image());
				dto_map.put("like_count", dto.getLike_count());
				if(dto.getLike_flag() > 0)
					dto_map.put("like_flag", true);
				else
					dto_map.put("like_flag", false);
				dto_map.put("review_contents", dto.getReview_contents());
				dto_map.put("rating_point", dto.getRating_point());
				dto_map.put("image_url", (List<String>)review_multi_map.get(dto.getReview_id()));
				dto_map.put("write_date", dto.getAdd_date());

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
	public void insertAppReview(AppReviewDto app_review_dto, ArrayList<FilePart> files) throws Exception {
		
			if(files != null) {
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
	}

	/**
	 *	리뷰 수정
	 */
	@Override
	public void reviewUpdate(AppReviewDto app_review_dto, ArrayList<FilePart> files) throws Exception{
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
			if(files!=null) {
				if(files.size() > 0)
					fileUpload(app_review_dto, files);
			}
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
	private void fileUpload(AppReviewDto app_review_dto, ArrayList<FilePart> files) throws Exception{
		List<Map<String, String>> attachment_list = Lists.newArrayList();
		
		if(files!=null) {
			int index = 0;
			for(FilePart uploadedFile : files){
				Map<String, String> attachment_map = Maps.newHashMap();
				String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
				Random rand = new Random(System.currentTimeMillis());

				String file_name = today + "_" + Math.abs(rand.nextInt(999999)+100000) + "." + FilenameUtils.getExtension(uploadedFile.filename());

				log.info(uploadedFile.filename());
				log.info("file : {}", System.getProperty("user.dir")+ IMAGE_DIRECTORY + "/" + file_name);
				File file = new File(System.getProperty("user.dir")+ IMAGE_DIRECTORY + "/" + file_name);
				uploadedFile.transferTo(file);

				attachment_map.put("attach_number", app_review_dto.getAttach_number());
				attachment_map.put("attach_name", file_name);
				attachment_map.put("attach_seq", String.valueOf(index++));
				attachment_map.put("attach_route", file.getName());
				attachment_map.put("attach_extension", FilenameUtils.getExtension(uploadedFile.filename()));
				attachment_map.put("attach_size", String.valueOf(0));
				attachment_map.put("review_user_id", app_review_dto.getReview_user_id());

				attachment_list.add(attachment_map);
			}
			log.info(attachment_list.toString());

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

	@Override
	public List<Map<String, Object>> selectMyReviewList(AppReviewDto review_dto) throws Exception{
		List<Map<String, Object>> review_list = new ArrayList<>();
		List<AppReviewDto> review_dto_list = app_review.selectMyReviewList(review_dto);

		for(AppReviewDto dto : review_dto_list){
			Map<String, Object> temp_map = new LinkedHashMap<>();
			temp_map.put("place_id", dto.getPlace_id());
			temp_map.put("place_name", dto.getPlace_name());
			temp_map.put("review_id", dto.getReview_id());
			temp_map.put("review_user_id", dto.getReview_user_id());
			temp_map.put("rating_avg", dto.getAppreview_rating());
			temp_map.put("rating", dto.getRating_point());
			temp_map.put("like_count", dto.getLike_count());
			if(dto.getLike_flag() > 0)
				temp_map.put("like_flag", true);
			else
				temp_map.put("like_flag", false);
			temp_map.put("category", dto.getCategory());
			temp_map.put("category_name", dto.getCategory_name());
			temp_map.put("write_date", dto.getAdd_date());
			review_list.add(temp_map);
		}

		return review_list;
	}

	@Override
	public void insertLikeReview(AppReviewDto app_review_dto) throws Exception{
		int count = app_review.selectLikeReview(app_review_dto);
		if(count < 1)
			app_review.insertLikeReview(app_review_dto);
		else
			throw new Exception("이미 좋아요하였습니다.");
	}

	public void deleteLikeReview(AppReviewDto app_review_dto) throws Exception{
		app_review.deleteLikeReview(app_review_dto);
	}
}
