package com.williamwigemo.spotify.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpotifyGetPlaylistTracksResponse extends SpotifyResult<SpotifyPlaylistTrackObject> {

    public SpotifyGetPlaylistTracksResponse(
            @JsonProperty("href") String href,
            @JsonProperty("limit") int limit,
            @JsonProperty("next") String next,
            @JsonProperty("offset") int offset,
            @JsonProperty("previous") String previous,
            @JsonProperty("total") int total,
            @JsonProperty("items") List<SpotifyPlaylistTrackObject> items) {
        super(href, limit, next, offset, previous, total, items);
    }
}
