package com.williamwigemo.trakt.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktMovieHistoryResult extends TraktWatchedResult<TraktMovie> {
    @JsonProperty("movie")
    private TraktMovie movie;

    public TraktMovie getMovie() {
        return movie;
    }
}
