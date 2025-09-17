package com.heatxd.quadtrivia.model;

import java.util.List;

public record TriviaQuestionModel(List<TriviaQuestion> questions) {
    public record TriviaQuestion(
            String type,
            String difficulty,
            String category,
            String question,
            List<String> answers,
            String token
    ) { }
}
