package com.williamwigemo.trakt;

import java.io.IOException;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TraktApiException extends Exception {
    private static class TraktApiErrorResponse {
        @JsonProperty("error")
        String error;

        @JsonProperty("error_description")
        String errorDescription;
    }

    private static String extractErrorMessage(HttpResponse<String> res) {
        TraktApiErrorResponse data;

        try {
            data = new ObjectMapper().readValue(res.body(), TraktApiErrorResponse.class);
        } catch (IOException e) {
            return "" + res.statusCode();
        }

        return data.error + ", " + data.errorDescription;
    }

    public TraktApiException(HttpResponse<String> res) {
        super("Unhandled error from Trakt API: " + extractErrorMessage(res));
    }

    public TraktApiException(String msg) {
        super("Unhandled error when sending data to Trakt API: " + msg);
    }
}
