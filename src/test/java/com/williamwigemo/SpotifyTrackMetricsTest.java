package com.williamwigemo;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.williamwigemo.entities.SpotifyTrackEntity;

public class SpotifyTrackMetricsTest {

    private final Set<SpotifyTrackEntity> trackSet;
    private final List<SpotifyTrackEntity> allTracks;

    public SpotifyTrackMetricsTest() {
        this.trackSet = new HashSet<>();
        this.allTracks = new ArrayList<>();
    }

    private void populateTracks(List<Integer> popularities) {
        for (Integer popularity : popularities) {
            SpotifyTrackEntity track = new SpotifyTrackEntity();
            track.setPopularity(popularity);
            track.setSpotifyUri(UUID.randomUUID().toString());
            this.trackSet.add(track);
            this.allTracks.add(track);
        }
    }

    @Test
    public void shouldReturnSoundtracks() {
        populateTracks(Arrays.asList(30, 50, 60));
        SpotifyTrackMetrics trackMetrics = new SpotifyTrackMetrics(trackSet);

        SpotifyTrackEntity track = allTracks.get(2);
        double score = trackMetrics.getSignificanceScore(track);
        assertTrue(score - 0.6 > 0.0);

        track = allTracks.get(0);
        score = trackMetrics.getSignificanceScore(track);
        assertTrue(score - 0.5 < 0.0);
        assertTrue(score - 0.3 > 0.0);
    }
}
