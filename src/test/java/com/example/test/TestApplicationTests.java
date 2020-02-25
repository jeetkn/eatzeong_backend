package com.example.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class TestApplicationTests {

	@Test
	void contextLoads() {
	}


	@Test
	public void test_flux_just_consumer(){
		List<String> names = new ArrayList<>();
		Flux<String> flux = Flux.just("에디킴", "아이린").log();

		flux.subscribe(names::add);
//		Assert.assertEquals(names, Arrays.asList("에디킴", "아이린"));
	}

}
