package com.williamwigemo.spotify;

import java.io.IOException;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpotifyApiException extends Exception {

    @JsonIgnoreProperties(ignoreUnknown = true)

    private static class Error {
        @JsonProperty("status")
        public int status;
        @JsonProperty("message")
        public String message;
    }

    private static class SpotifyApiErrorResponse {
        @JsonProperty("error")
        Error error;
    }

    private static String extractErrorMessage(HttpResponse<String> res) {
        SpotifyApiErrorResponse data;

        try {
            data = new ObjectMapper().readValue(res.body(), SpotifyApiErrorResponse.class);
        } catch (IOException e) {
            return "" + res.statusCode();
        }

        Error error = data.error;
        return error.status + ", " + error.message;
    }

    public SpotifyApiException(HttpResponse<String> res) {
        super("Unhandled error from Spotify WebAPI: Status code " + extractErrorMessage(res));
    }

    public SpotifyApiException(String msg) {
        super("Unhandled error when sending data to Spotify WebAPI: " + msg);
    }
}
