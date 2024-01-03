package com.williamwigemo;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.williamwigemo.spotify.SpotifyArtist;
import com.williamwigemo.spotify.SpotifyTrack;

public class TrackSimilarityTest {
    @Test
    public void shouldReturnCorrect() {
        SpotifyTrack track = new SpotifyTrack();
        track.setName("My Heart Will Go On - Love Theme from \"Titanic\"");
        SpotifyArtist spotifyArtist = new SpotifyArtist();
        spotifyArtist.name = "Céline Dion";
        track.getArtists().add(spotifyArtist);

        assertTrue(TrackSimilarity.isSameTrack(track, "My Heart Will Go On", null) == false);
        assertTrue(TrackSimilarity.isSameTrack(track, "My Heart Will Go On", "Céline Dion"));
    }
}
