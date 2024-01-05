package com.williamwigemo.spotify.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyAddPlaylistResponse extends SpotifyPlaylist {

}
