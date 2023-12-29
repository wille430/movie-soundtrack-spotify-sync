package com.williamwigemo.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {
    @JsonProperty("uri")
    public String uri;

    @JsonProperty("name")
    public String name;

    @JsonProperty("popularity")
    public int popularity;

    @JsonProperty("artists")
    public List<SpotifyArtist> artists;

    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }
}
