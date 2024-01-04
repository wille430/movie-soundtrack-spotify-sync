package com.williamwigemo;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import com.williamwigemo.entities.SpotifyTrackEntity;

public class SpotifyTrackMetrics implements TrackMetrics<SpotifyTrackEntity> {

    private static final double RelPopularityWeight = 0.45;
    private static final double DefaultMaxPopularity = 50.0;

    private static final int MinPopularity = 64;
    private static final double MinSignificanceScore = 0.7;

    private Set<SpotifyTrackEntity> allTracks;
    private Double maxPopularity;

    public SpotifyTrackMetrics(Set<SpotifyTrackEntity> allTracks) {
        this.allTracks = allTracks;
    }

    private double getMaxPopularity() {
        if (this.maxPopularity == null) {
            Optional<SpotifyTrackEntity> mostPopular = allTracks.stream()
                    .sorted(Comparator.comparingInt(track -> ((SpotifyTrackEntity) track).getPopularity()).reversed())
                    .findFirst();

            if (mostPopular.isPresent()) {
                this.maxPopularity = (double) mostPopular.get().getPopularity();
            } else {
                this.maxPopularity = DefaultMaxPopularity;
            }
        }

        return this.maxPopularity;
    }

    public boolean isSignificant(SpotifyTrackEntity track) {
        return this.getSignificanceScore(track) >= MinSignificanceScore
                && track.getPopularity() >= MinPopularity;
    }

    @Override
    public double getSignificanceScore(SpotifyTrackEntity track) {
        double relativePopularity = track.getPopularity() / this.getMaxPopularity();
        double popularity = track.getPopularity() / 100.0;

        return (RelPopularityWeight * relativePopularity) + ((1 - RelPopularityWeight) * popularity);
    }
}
