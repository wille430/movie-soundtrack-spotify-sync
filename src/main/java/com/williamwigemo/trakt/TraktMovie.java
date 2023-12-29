package com.williamwigemo.trakt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktMovie {
    @JsonProperty("title")
    public String title;

    @JsonProperty("year")
    public int year;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TraktMovieIds {
        @JsonProperty("imdb")
        public String imdb;
    }

    @JsonProperty("ids")
    public TraktMovieIds ids;

    @Override
    public int hashCode() {
        return this.ids.imdb.hashCode();
    }
}
