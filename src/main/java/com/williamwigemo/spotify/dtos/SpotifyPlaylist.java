package com.williamwigemo.spotify.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.williamwigemo.entities.PlaylistEntity;

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

    public PlaylistEntity toEntity() {
        PlaylistEntity entity = new PlaylistEntity();

        entity.setPlaylistName(name);
        entity.setSpotifyId(id);

        return entity;
    }
}
