package com.williamwigemo.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyGetPlaylistsResponse extends SpotifyResult<SpotifyPlaylist> {

    public SpotifyGetPlaylistsResponse(
            @JsonProperty("href") String href,
            @JsonProperty("limit") int limit,
            @JsonProperty("next") String next,
            @JsonProperty("offset") int offset,
            @JsonProperty("previous") String previous,
            @JsonProperty("total") int total,
            @JsonProperty("items") List<SpotifyPlaylist> items) {
        super(href, limit, next, offset, previous, total, items);
    }
    // public List<SpotifyPlaylist> items;

    // public SpotifyGetPlaylistsResponse(
    // @JsonProperty("items") List<SpotifyAddPlaylistResponse> items) {
    // this.items = items.stream().map(o -> (SpotifyPlaylist) o).toList();
    // }
}
