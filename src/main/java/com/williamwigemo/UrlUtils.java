package com.williamwigemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UrlUtils {
    public static Map<String, String> queryToMap(String query) {
        String[] pairs = query.split("&");
        Map<String, String> queryMap = new HashMap<>();

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                queryMap.put(keyValue[0], keyValue[1]);
            }
        }

        return queryMap;
    }

    public static <T> String hashMapToString(Map<String, T> hashMap) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(hashMap);
    }

    public static <T> T parseResponseBody(String responseBody, Class<T> cl) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(responseBody, cl);
        } catch (JsonProcessingException e) {
            throw new IOException("Error parsing response body", e);
        }
    }
}
