package com.williamwigemo.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyArtist {
    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;
}
