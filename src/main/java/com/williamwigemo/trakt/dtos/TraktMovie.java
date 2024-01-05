package com.williamwigemo.trakt.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.williamwigemo.entities.MediaEntity;

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

    public MediaEntity toEntity() {
        MediaEntity entity = new MediaEntity();

        entity.setImdbId(this.ids.imdb);
        entity.setLastFetchedSoundtracks(0L);
        entity.setTitle(this.title);
        entity.setYear(this.year);

        return entity;
    }
}
