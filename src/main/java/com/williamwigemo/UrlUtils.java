package com.williamwigemo;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public static String ISO8601DatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final SimpleDateFormat isoDateFormat = new SimpleDateFormat(ISO8601DatePattern);

    public static Date parseISO8601Date(String dateStr) throws ParseException {
        return isoDateFormat.parse(dateStr);
    }

    public static String getISO8601Date(Date startAt) {
        Instant instant = Instant.ofEpochMilli(startAt.getTime());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ISO8601DatePattern);
        return dateTime.format(formatter);
    }

    public static String getQueryString(HashMap<String, String> params) {
        return params.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    public static String getRandomString(int i) {
        return UUID.randomUUID().toString().substring(0, 16);
    }
}
