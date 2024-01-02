package com.williamwigemo.trakt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.williamwigemo.entities.MediaEntity;
import com.williamwigemo.trakt.TraktMovie.TraktMovieIds;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktShow {
    @JsonProperty("title")
    private String title;

    @JsonProperty("year")
    private int year;

    @JsonProperty("ids")
    private TraktMovieIds ids;

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public TraktMovieIds getIds() {
        return ids;
    }

    public MediaEntity toEntity() {
        MediaEntity mediaEntity = new MediaEntity();

        mediaEntity.setImdbId(ids.imdb);
        mediaEntity.setLastFetchedSoundtracks(0L);
        mediaEntity.setTitle(title);
        mediaEntity.setYear(year);

        return mediaEntity;
    }
}
