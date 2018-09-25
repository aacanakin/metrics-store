package model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    @JsonProperty("message")
    private final String message;

    @JsonCreator
    public ErrorResponse(@JsonProperty("message") String message) {
        this.message = message;
    }
}
