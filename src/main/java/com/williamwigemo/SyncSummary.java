package com.williamwigemo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.williamwigemo.entities.MediaEntity;
import com.williamwigemo.entities.SpotifyTrackEntity;

public class SyncSummary {
    public static void printSummary(List<MediaEntity> history,
            Map<MediaEntity, Set<SpotifyTrackEntity>> movieToTracksMap,
            Set<SpotifyTrackEntity> allTracksAdded) {
        // print summary
        System.out.println("Tracks added: " + allTracksAdded.size());
        System.out.println("Tracks already in playlist: " + (history.size() - allTracksAdded.size()));
        System.out.println("Number of movies and shows: " + history.size());
        System.out.println();

        for (MediaEntity movie : movieToTracksMap.keySet()) {
            Set<SpotifyTrackEntity> tracks = movieToTracksMap.get(movie);
            Set<SpotifyTrackEntity> tracksAdded = new HashSet<>();

            for (SpotifyTrackEntity track : tracks) {
                if (allTracksAdded.contains(track)) {
                    tracksAdded.add(track);
                }
            }

            if (tracksAdded.size() == 0) {
                continue;
            }

            System.out.print(movie.getTitle() + " (" + movie.getYear() + "): ");

            System.out.println("Added " + tracksAdded.size() + " tracks");

            for (SpotifyTrackEntity track : tracksAdded) {
                String collaborators = String.join(", ", track.getCollaborators().stream().limit(3).toList());
                System.out.println("   - " + collaborators + ": " + track.getTrackName());
            }
            System.out.println();
        }
    }
}
