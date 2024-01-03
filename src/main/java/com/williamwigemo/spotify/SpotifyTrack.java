package com.williamwigemo.spotify;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.williamwigemo.entities.SpotifyTrackEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyTrack {
    @JsonProperty("uri")
    public String uri;

    @JsonProperty("name")
    public String name;

    @JsonProperty("popularity")
    public int popularity;

    @JsonProperty("artists")
    public List<SpotifyArtist> artists = new ArrayList<>();

    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public List<SpotifyArtist> getArtists() {
        return artists;
    }

    public void setArtists(List<SpotifyArtist> artists) {
        this.artists = artists;
    }

    public SpotifyTrackEntity toEntity() {
        SpotifyTrackEntity entity = new SpotifyTrackEntity();

        entity.setPopularity(getPopularity());
        entity.setSpotifyUri(getUri());
        entity.setCollaborators(getArtists().stream().map(o -> o.name).toList());
        entity.setTrackName(getName());

        return entity;
    }
}
