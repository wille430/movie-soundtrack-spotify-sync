package com.williamwigemo.spotify;

import java.net.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    private static class SpotifyApiAuthErrorResponse {
        @JsonProperty("error")
        public String error;

        @JsonProperty("error_description")
        public String errorDescription;
    }

    private static <T> T tryExtractErrorMessage(HttpResponse<String> res, Class<T> cls) {
        try {
            return new ObjectMapper().readValue(res.body(), cls);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static String extractErrorMessage(HttpResponse<String> res) {
        SpotifyApiErrorResponse data = tryExtractErrorMessage(res, SpotifyApiErrorResponse.class);
        if (data != null) {
            Error error = data.error;
            return error.status + ", " + error.message;
        }

        SpotifyApiAuthErrorResponse authErr = tryExtractErrorMessage(res, SpotifyApiAuthErrorResponse.class);
        if (authErr != null) {
            return String.format("%s: %s", authErr.error, authErr.errorDescription);
        }

        return "" + res.statusCode();
    }

    public SpotifyApiException(HttpResponse<String> res) {
        super("Unhandled error from Spotify WebAPI: Status code " + extractErrorMessage(res));
    }

    public SpotifyApiException(String msg) {
        super("Unhandled error when sending data to Spotify WebAPI: " + msg);
    }
}
