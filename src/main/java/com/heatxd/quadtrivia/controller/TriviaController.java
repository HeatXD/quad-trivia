package com.heatxd.quadtrivia.controller;

import com.heatxd.quadtrivia.dto.CategoryResponse;
import com.heatxd.quadtrivia.model.TriviaQuestionModel;
import com.heatxd.quadtrivia.service.TriviaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/trivia")
public class TriviaController {
    private final TriviaService triviaService;

    public TriviaController(TriviaService service) {
        this.triviaService = service;
    }

    @GetMapping("/categories")
    public Mono<CategoryResponse> getCategories() {
        return triviaService.getCategories();
    }

    @GetMapping("/token")
    public Mono<String> getSessionToken(WebSession session) {
        return triviaService.getSessionToken(session);
    }

    @GetMapping("/questions")
    public Mono<TriviaQuestionModel> getQuestions(
            WebSession session,
            @RequestParam(defaultValue = "10") int amount,
            @RequestParam(defaultValue = "0") int category,
            @RequestParam(defaultValue = "") String difficulty
    ) {
        return triviaService.getQuestions(session, amount, category, difficulty);
    }

    @GetMapping("/validate")
    public Mono<Boolean> checkAnswer(
            @RequestParam String token,
            @RequestParam String instant,
            @RequestParam String answer
    ) {
        return triviaService.checkAnswer(token, instant, answer);
    }
}
