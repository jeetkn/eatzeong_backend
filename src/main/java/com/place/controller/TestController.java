package com.place.controller;

import com.place.dto.Dto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/test")
@CrossOrigin(origins = {"*"})
@PropertySource("classpath:application.properties")
@Slf4j
public class TestController {

    @Value("${image.review}")
    private String image_directory;

    @PostMapping(value = "/fileupload")
    public Dto<String> test(){
        Dto<String> return_dto = new Dto<>();
        return_dto.setDataList("asdfasdf");
//		return parts
//				.filter(part -> part instanceof FilePart) // only retain file parts
//				.ofType(FilePart.class) // convert the flux to FilePart
//				.flatMap(this::saveFile); // save each file and flatmap it to a flux of results
        return return_dto;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> requestBodyFlux(@RequestPart("file") List<FilePart> fileParts) throws IOException {
        fileParts.stream()
                .forEach(filePart -> {
                    System.out.println(filePart.filename());
                    try {
                        Path tempFile = Files.createTempFile("test", filePart.filename());
                        filePart.transferTo(tempFile.toFile());
                        System.out.println(tempFile.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        log.info("현재 디렉토리 : {}",System.getProperty("user.dir"));

//        Path tempFile2 = Files.createFile(now);


        //NOTE 방법 1
//        AsynchronousFileChannel channel =
//                AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
//        DataBufferUtils.write(filePart.content(), channel, 0)
//                .doOnComplete(() -> {
//                    System.out.println("finish");
//                })
//                .subscribe();

        //NOTE 방법 2
//        filePart.transferTo(tempFile.toFile());

//        System.out.println(tempFile.toString());
        return Mono.just(fileParts.get(0).filename());
    }

    public static final String uploadingDir = System.getProperty("user.dir") + "/uploadingDir/";

    @PostMapping(value = "/uploadtest")
    public List<String> uploadingPost(@RequestParam("file") MultipartFile[] uploadingFiles,
            @Value("${test.string}") String str) throws IOException {
        List<String> return_list = new ArrayList<>();
        for(MultipartFile uploadedFile : uploadingFiles) {
            log.info(str);
            File file = new File(uploadingDir + uploadedFile.getOriginalFilename());

            uploadedFile.transferTo(file);
            log.info(file.getName());
            return_list.add(file.getName());
        }

        return return_list;
    }

    @PutMapping(value="/{uploactest2}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<String> save(@RequestPart("file") ArrayList<FilePart> files) throws IOException {
        List<String> filename_list = new ArrayList<>();

        log.info(System.getProperty("user.dir")+ image_directory);

        files.stream()
                .forEach(uploadedFile -> {
                    log.info(uploadedFile.filename());
                    File file = new File(System.getProperty("user.dir")+ image_directory + uploadedFile.filename());
                    uploadedFile.transferTo(file);
                    filename_list.add(file.getName());
                });

        return filename_list;
    }

//    @GetMapping(value = "/review/{file_name}", produces = MediaType.IMAGE_JPEG_VALUE)
//    public byte[] find(@PathVariable String file_name) throws IOException {
//
//        String file_path = System.getProperty("user.dir")+ image_directory + "/" + file_name;
//        log.info("file : {} ", file_path);
//        InputStream in = getClass().getResourceAsStream(file_path);
//        return IOUtils.toByteArray(in);
//    }

    @GetMapping(value = "/review/{file_name}", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<Resource> getReviewImage(@PathVariable String file_name) throws IOException {

        ResourceLoader resourceLoader = new DefaultResourceLoader();

        String file_path = System.getProperty("user.dir")+ image_directory + "/" + file_name;
        log.info("file : {} ", file_path);

        return Mono.fromSupplier(() ->
                resourceLoader.getResource("file:" + file_path));
    }

    @GetMapping(value = "/time")
    public String getTime(){

        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS (z Z)");
        String format_time = format.format (System.currentTimeMillis());

        log.info("현재 시간 : {}", format_time);

        return format_time;
    }
}


