package com.heatxd.quadtrivia.service;

import com.heatxd.quadtrivia.dto.CategoryResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TriviaService {
    private final WebClient webClient;

    public TriviaService(@Qualifier("triviaClient") WebClient client) {
        this.webClient = client;
    }

    @Cacheable("categories")
    public Mono<CategoryResponse> getCategories() {
        return webClient.get()
                .uri("/api_category.php")
                .retrieve()
                .bodyToMono(CategoryResponse.class);
    }
}
