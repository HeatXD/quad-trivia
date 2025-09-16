package com.heatxd.quadtrivia.service;

import com.heatxd.quadtrivia.dto.CategoryResponse;
import com.heatxd.quadtrivia.dto.TriviaQuestionResponse;
import com.heatxd.quadtrivia.dto.TriviaTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
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

    public Mono<TriviaQuestionResponse> getQuestions() {
        return Mono.empty();
    }

    public Mono<String> getSessionToken(WebSession session) {
        String existingToken = (String) session.getAttributes().get("triviaToken");
        Instant createdAt = (Instant) session.getAttributes().get("triviaTokenCreatedAt");

        // force token refresh after 3 hours of use.
        boolean expired = createdAt == null || Duration.between(createdAt, Instant.now()).toHours() >= 3;
        if (existingToken != null && !expired) {
            return Mono.just(existingToken);
        }

        return webClient.get()
                .uri("/api_token.php?command=request")
                .retrieve()
                .bodyToMono(TriviaTokenResponse.class)
                .map(TriviaTokenResponse::token)
                .doOnNext(token -> {
                    session.getAttributes().put("triviaToken", token);
                    session.getAttributes().put("triviaTokenCreatedAt", Instant.now());
                })
                .onErrorResume(e -> {
                    System.err.println("Failed to fetch OpenTDB token: " + e.getMessage());
                    return Mono.empty();
                });
    }
}
