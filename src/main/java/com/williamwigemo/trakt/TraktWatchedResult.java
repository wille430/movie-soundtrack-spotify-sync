package com.williamwigemo.trakt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktWatchedResult<S> {
    @JsonProperty("plays")
    private int plays;

    @JsonProperty("last_updated_at")
    private String lastUpdatedAt;

    public int getPlays() {
        return plays;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
