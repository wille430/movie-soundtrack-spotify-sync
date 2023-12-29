package com.williamwigemo.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylist {
    @JsonProperty("collaborative")
    public boolean collaborative;

    @JsonProperty("description")
    public String description;

    @JsonProperty("href")
    public String href;

    @JsonProperty("id")
    public String id;

    @JsonProperty("name")
    public String name;
}
