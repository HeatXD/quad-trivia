package com.heatxd.quadtrivia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CategoryResponse(@JsonProperty("trivia_categories") List<TriviaCategory> triviaCategories) {
    private record TriviaCategory(int id, String name) {}
}
