package com.williamwigemo;

import com.williamwigemo.spotify.SpotifyTrack;

public class TrackSimilarity {
    public static boolean isSameTrack(SpotifyTrack track, String trackName, String artist) {
        if (trackName == null || artist == null)
            return false;

        boolean containsArtist = track.artists.stream()
                .anyMatch(p -> p.name.toLowerCase().contains(artist.toLowerCase()));
        return containsArtist && track.getName().toLowerCase().contains(trackName.toLowerCase());
    }
}
