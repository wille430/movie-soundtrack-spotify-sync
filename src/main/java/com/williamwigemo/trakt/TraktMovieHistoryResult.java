package com.williamwigemo.trakt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktMovieHistoryResult {
    @JsonProperty("plays")
    public int plays;

    @JsonProperty("movie")
    public TraktMovie movie;

    @JsonProperty("last_updated_at")
    public String lastUpdatedAt;
}
