package com.heatxd.quadtrivia.service;

import com.heatxd.quadtrivia.dto.CategoryResponse;
import com.heatxd.quadtrivia.dto.TriviaQuestionResponse;
import com.heatxd.quadtrivia.dto.TriviaTokenResponse;
import com.heatxd.quadtrivia.model.TriviaQuestionModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class TriviaService {
    private final WebClient webClient;
    private final String triviaSecret;

    public TriviaService(@Qualifier("triviaClient") WebClient client) {
        this.webClient = client;
        this.triviaSecret = genTriviaSecret();
    }

    private String genTriviaSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Cacheable("categories")
    public Mono<CategoryResponse> getCategories() {
        return webClient.get()
                .uri("/api_category.php")
                .retrieve()
                .bodyToMono(CategoryResponse.class)
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<TriviaQuestionResponse> fetchQuestions (WebSession session, int amount, int category, String difficulty) {
        return getSessionToken(session)
                .flatMap(token -> webClient.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder.path("/api.php")
                                    .queryParam("amount", amount)
                                    .queryParam("token", token);
                            if (category > 0) builder.queryParam("category", category);
                            if (difficulty != null && !difficulty.isEmpty()) builder.queryParam("difficulty", difficulty);
                            // System.out.println("Fetching questions with URI: " + uriBuilder.build());
                            return builder.build();
                        })
                        .retrieve()
                        .bodyToMono(TriviaQuestionResponse.class)
                )
                .onErrorResume(e -> {
                    System.err.println("Failed to fetch OpenTDB Questions: " + e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<TriviaQuestionModel> getQuestions(WebSession session, int amount, int category, String difficulty) {
        return fetchQuestions(session, amount, category, difficulty)
                .map( response -> {
                    var questionList = Optional.of(response.results()).orElse(Collections.emptyList());
                    var questions = questionList.stream()
                            .map(q -> {
                                // combine correct + incorrect answers
                                var answers = new ArrayList<String>();
                                answers.add(q.correctAnswer());
                                answers.addAll(q.incorrectAnswers());
                                // shuffle
                                Collections.shuffle(answers);
                                // generate a one-way hmac token for the correct answer
                                HMACResult result = genToken(q.correctAnswer());
                                return new TriviaQuestionModel.TriviaQuestion(
                                        q.type(),
                                        q.difficulty(),
                                        q.category(),
                                        q.question(),
                                        answers,
                                        result.token(),
                                        result.instant()
                                );
                            })
                            .toList();
                    return new TriviaQuestionModel(questions);
                })
                .onErrorResume(e -> {
                    System.err.println("Failed to convert OpenTDB Questions: " + e.getMessage());
                    return Mono.empty();
                });
    }

    private HMACResult genToken(String answer) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(triviaSecret.getBytes(), "HmacSHA256"));
            String instant = Instant.now().toString();
            String payload = answer + instant;
            byte[] hmac = mac.doFinal(payload.getBytes());
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
            return new HMACResult(token, instant);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public Mono<Boolean> checkAnswer(String token, String instant, String answer) {
        try {
            // recreate the HMAC from the submitted answer using the same secret
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(triviaSecret.getBytes(), "HmacSHA256"));
            String payload = answer + instant;
            byte[] hmac = mac.doFinal(payload.getBytes());
            String generatedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
            // compare generated token with the token sent from the client
            return Mono.just(generatedToken.equals(token));
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to validate answer", e));
        }
    }

    private record HMACResult(String token, String instant) {}
}
