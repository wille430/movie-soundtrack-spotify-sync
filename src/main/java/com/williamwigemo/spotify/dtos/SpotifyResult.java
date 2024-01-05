package com.williamwigemo.spotify.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyResult<T> {
    public String href;
    public int limit;
    public String next;
    public int offset;
    public String previous;
    public int total;
    public List<T> items;

    public SpotifyResult(
            @JsonProperty("href") String href,
            @JsonProperty("limit") int limit,
            @JsonProperty("next") String next,
            @JsonProperty("offset") int offset,
            @JsonProperty("previous") String previous,
            @JsonProperty("total") int total,
            @JsonProperty("items") List<T> items) {
        this.href = href;
        this.limit = limit;
        this.next = next;
        this.offset = offset;
        this.previous = previous;
        this.total = total;
        this.items = items;
    }
}
