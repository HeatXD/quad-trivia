package com.heatxd.quadtrivia.controller;

import com.heatxd.quadtrivia.dto.CategoryResponse;
import com.heatxd.quadtrivia.service.TriviaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
