package com.williamwigemo;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.williamwigemo.spotify.SpotifyTrack;

public class SpotifyTrackMetrics implements TrackMetrics<SpotifyTrack> {

    private static final double RelPopularityWeight = 0.45;
    private static final double DefaultMaxPopularity = 50.0;
    private Set<SpotifyTrack> allTracks;
    private Double maxPopularity;

    public SpotifyTrackMetrics(Set<SpotifyTrack> allTracks) {
        this.allTracks = allTracks;
    }

    private double getMaxPopularity() {
        if (this.maxPopularity == null) {
            Optional<SpotifyTrack> mostPopular = allTracks.stream()
                    .sorted(Comparator.comparingInt(track -> ((SpotifyTrack) track).popularity).reversed())
                    .findFirst();

            if (mostPopular.isPresent()) {
                this.maxPopularity = (double) mostPopular.get().popularity;
            } else {
                this.maxPopularity = DefaultMaxPopularity;
            }
        }

        return this.maxPopularity;
    }

    @Override
    public double getSignificanceScore(SpotifyTrack track) {
        double relativePopularity = track.popularity / this.getMaxPopularity();
        double popularity = track.popularity / 100.0;

        return (RelPopularityWeight * relativePopularity) + ((1 - RelPopularityWeight) * popularity);
    }
}
