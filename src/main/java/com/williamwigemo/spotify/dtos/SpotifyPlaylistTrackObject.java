package com.williamwigemo.spotify.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyPlaylistTrackObject {
    @JsonProperty("track")
    public SpotifyTrack track;
}
