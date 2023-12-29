package com.williamwigemo.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifySearchResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Album {
        public String href;

        @JsonCreator
        public Album(
                @JsonProperty("href") String href) {
            this.href = href;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tracks {
        public List<SpotifyTrack> trackObjects;

        @JsonCreator
        public Tracks(
                @JsonProperty("items") List<SpotifyTrack> trackObjects) {
            this.trackObjects = trackObjects;
        }
    }

    public final Tracks tracks;

    @JsonCreator
    public SpotifySearchResponse(
            @JsonProperty("tracks") Tracks tracks) {
        this.tracks = tracks;
    }
}
