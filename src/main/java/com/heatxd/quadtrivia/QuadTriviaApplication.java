package com.heatxd.quadtrivia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QuadTriviaApplication {
	public static void main(String[] args) {
		SpringApplication.run(QuadTriviaApplication.class, args);
	}
}
