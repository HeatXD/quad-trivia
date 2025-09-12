package com.heatxd.quadtrivia.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TriviaService {
    private final WebClient webClient;

    public TriviaService(@Qualifier("triviaClient") WebClient client) {
        this.webClient = client;
    }
}
