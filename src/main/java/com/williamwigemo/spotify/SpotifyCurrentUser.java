package com.williamwigemo.spotify;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyCurrentUser {

    public String id;

    public SpotifyCurrentUser(
            @JsonProperty("id") String id) {
        this.id = id;
    }
}
