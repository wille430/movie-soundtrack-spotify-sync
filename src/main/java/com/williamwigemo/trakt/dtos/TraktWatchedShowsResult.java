package com.williamwigemo.trakt.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TraktWatchedShowsResult extends TraktWatchedResult<TraktShow> {
    @JsonProperty("show")
    private TraktShow show;

    public TraktShow getShow() {
        return show;
    }
}
