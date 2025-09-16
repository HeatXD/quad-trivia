package com.heatxd.quadtrivia.service;

import com.heatxd.quadtrivia.dto.CategoryResponse;
import com.heatxd.quadtrivia.dto.TriviaTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

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
                .bodyToMono(CategoryResponse.class)
                .onErrorResume(e -> Mono.just(new CategoryResponse(List.of())));
    }

    public Mono<String> getSessionToken(WebSession session) {
        // first check if we already have a token
        String existing =  (String) session.getAttributes().get("triviaToken");
        if (existing != null) {
            return Mono.just(existing);
        }

        return webClient.get()
                .uri("/api_token.php?command=request")
                .retrieve()
                .bodyToMono(TriviaTokenResponse.class)
                .map(TriviaTokenResponse::token)
                .doOnNext(token -> session.getAttributes().put("triviaToken", token))
                .onErrorResume(e -> {
                    System.err.println("Failed to fetch OpenTDB token: " + e.getMessage());
                    return Mono.empty();
                });
    }
}
