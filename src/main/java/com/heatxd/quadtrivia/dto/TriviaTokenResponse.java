package com.heatxd.quadtrivia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TriviaTokenResponse(
        @JsonProperty("response_code") int responseCode,
        @JsonProperty("response_message") String responseMessage,
        String token) {
}
