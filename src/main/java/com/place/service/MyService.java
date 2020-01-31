package com.place.service;

import com.google.common.collect.Maps;
import com.place.api.CommonApi;
import com.place.api.google.GoogleCustomSearch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service("com.place.service.MyService")
@Transactional
@Slf4j
@EnableAsync
public class MyService {
    static final String URL1 = "http://localhost:8090/service1?req={req}";
    static final String URL2 = "http://localhost:8090/service2?req={req}";
//    WebClient client = WebClient.create();
    @Autowired
    WebClient.Builder builder;

    @Inject
    GoogleCustomSearch customSearch;

    @Async
    public CompletableFuture<String> work(String req) {
        return CompletableFuture.completedFuture(req + "/work");
    }

    @Async
    public Mono<String> async(int idx) throws Exception {
        log.info("query {}", idx);
        WebClient client = builder.build();
        Map<String, String> param = Maps.newHashMap();

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String last_year_date = LocalDateTime.now().minusYears(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String naver_search_cx = "007124061159672905157:4dldrdpppep";

//        param.put("q", query);
//        param.put("sort", "date:r:" + last_year_date + ":" + today); // 최근 2년


        String URL = "https://www.googleapis.com/customsearch/v1/siterestrict?q=홍대 맛집&sort=date:r:20180130:20200130&key=AIzaSyDszKM3HX1Ec-XIFb7MWMd6detznF1zAGc&cx=007124061159672905157:4dldrdpppep";
        log.info("URL {}", URL);

//        log.info("pos1");
//        Mono<String> m = webClient.get().uri(URL)
//                .retrieve()
//                .bodyToMono(String.class)
//                .flatMap(res2 -> Mono.fromCompletionStage(work(res2)))
//                .doOnNext(c -> log.info(c.toString()))
//                .log()
//                .flatMap(s -> Mono.fromCompletionStage(CompletableFuture.completedFuture(s)))
//                .doOnNext(c -> log.info(c.toString()));
//        log.info("pos2");
//        System.out.println(m.block());

//        webClient.get().uri(URL2, idx)
//                .exchange()
//                .flatMap(c -> c.bodyToMono(String.class))
////                .doOnNext(c -> log.info(c.toString()))
//                .flatMap(res2 -> Mono.fromCompletionStage(work(res2)))
//                .doOnNext(c -> {
//                    log.info(c.toString());
//                    map.put("mono2", c);
//                })
//                .subscribe();

        Mono<String> resultMono = client.get()
                .uri(URL1, idx)
                .exchange()
                .flatMap(  															// bodyToMono가 Mono<T>를 반환하므로 flatMap을 써야함
                        clientResponse -> clientResponse.bodyToMono(String.class))  // 결과 Mono<String>
                .doOnNext(c -> log.info(c))  										// log로 Thread 이름 확인 - A
                // 아래와 같이 flatMap()을 통해 chaining하면 nonblocking-async를 순차적으로 처리 가능
                .flatMap(res1 -> client.get().uri(URL2, res1).exchange())  			// 결과 Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class))  							// 결과 Mono<String>
                .doOnNext(c -> log.info(c))  										// log로 Thread 이름 확인 - A
                .flatMap(res2 -> Mono.fromCompletionStage(work(res2)))  	// 결과 Mono<String>
                .doOnNext(c -> log.info(c))
                .log();  										// myService.work()를 실행하는 Thread 이름 확인 - B

        return resultMono;
    }
}
