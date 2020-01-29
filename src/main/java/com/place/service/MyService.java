package com.place.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

@Service("com.place.service.MyService")
@Transactional
@Slf4j
@EnableAsync
public class MyService {
    @Async
    public CompletableFuture<String> work(String req) {
        return CompletableFuture.completedFuture(req + "/work");
    }
}
