package com.place.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@CrossOrigin(origins = {"*"})
@PropertySource("classpath:application.properties")
@Slf4j
public class ImageController {

    @Value("${image.review}")
    private String REVIEW_IMAGE_DIRECTORY;

    @Value("${image.profile}")
    private String PROFILE_IMAGE_DIRECTORY;

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * 리뷰 이미지 가져온다.
     * @param file_name
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/review/{file_name}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public Mono<Resource> getReviewImage(@PathVariable String file_name) throws IOException {

        String file_path = System.getProperty("user.dir")+ REVIEW_IMAGE_DIRECTORY + "/" + file_name;
        log.info("file : {} ", file_path);

        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + file_path));
    }

    /**
     * 프로필 사진을 가져온다.
     * @param file_name
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/profile/{file_name}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public Mono<Resource> getProfileImage(@PathVariable String file_name) throws IOException {

        String file_path = System.getProperty("user.dir")+ PROFILE_IMAGE_DIRECTORY + "/" + file_name;
        log.info("file : {} ", file_path);

        return Mono.fromSupplier(() -> resourceLoader.getResource("file:" + file_path));
    }
}
