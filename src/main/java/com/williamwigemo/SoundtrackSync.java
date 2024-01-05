package com.williamwigemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.williamwigemo.entities.MediaEntity;
import com.williamwigemo.entities.SpotifyTrackEntity;
import com.williamwigemo.spotify.SpotifyAPI;
import com.williamwigemo.spotify.SpotifyApiException;
import com.williamwigemo.spotify.SpotifyPlaylistSync;
import com.williamwigemo.trakt.TraktApi;
import com.williamwigemo.trakt.TraktApiException;

import me.tongfei.progressbar.ProgressBar;

public class SoundtrackSync {
    private final SpotifyAPI spotifyAPI;
    private final TraktApi traktApi;
    private final MediaService mediaService;

    public SoundtrackSync() throws IOException {
        this.spotifyAPI = new SpotifyAPI();
        this.traktApi = new TraktApi();
        this.mediaService = new MediaService(this.spotifyAPI);
    }

    private Map<MediaEntity, Set<SpotifyTrackEntity>> loadSoundtracks(List<MediaEntity> history,
            Set<String> soundtrackNames)
            throws IOException, SpotifyApiException {

        Map<MediaEntity, Set<SpotifyTrackEntity>> imdbIdToTracks = new HashMap<>();
        for (MediaEntity entity : ProgressBar.wrap(history, "Fetching soundtracks...")) {
            entity = this.mediaService.fetchMedia(entity);
            imdbIdToTracks.put(entity, entity.getSoundtracks());
        }

        return imdbIdToTracks;
    }

    public void syncAll() {
        Date lastMovieSync = new Date();

        try {
            this.traktApi.authenticate();
        } catch (TraktApiException | IOException e) {
            System.out.println("Failed to authenticate with Trakt.");
            e.printStackTrace();
            return;
        }
        try {
            this.spotifyAPI.authenticate();
        } catch (SpotifyApiException | IOException e) {
            System.out.println("Failed to authenticate with Spotify.");
            e.printStackTrace();
            return;
        }

        // load watched movies
        List<MediaEntity> history = new ArrayList<>();
        try {
            this.traktApi.getWatchedMovies().stream().map(o -> o.toEntity()).forEach(history::add);
            this.traktApi.getShowsHistory().stream().map(o -> o.getShow().toEntity()).forEach(history::add);
        } catch (TraktApiException e) {
            System.out.println("Failed to load shows and movie history from Trakt.");
            e.printStackTrace();
            return;
        }

        // load soundtracks to respective movie
        Set<String> soundtrackNames = new HashSet<>();

        Map<MediaEntity, Set<SpotifyTrackEntity>> movieToTracksMap;
        try {
            movieToTracksMap = loadSoundtracks(history, soundtrackNames);
        } catch (SpotifyApiException | IOException e) {
            System.out.println("Failed to load soundtracks from Spotify.");
            e.printStackTrace();
            return;
        }

        // create/update spotify playlist
        SpotifyPlaylistSync playlistSync = new SpotifyPlaylistSync(spotifyAPI);

        System.out.println("Populating playlist with new soundtracks...");
        Set<SpotifyTrackEntity> tracksAdded;
        try {
            tracksAdded = playlistSync.updateMovies(movieToTracksMap);
        } catch (SpotifyApiException e) {
            System.out.println("Failed adding soundtracks to Spotify playlist.");
            e.printStackTrace();
            return;
        }

        this.traktApi.setLastMovieSync(lastMovieSync);

        SyncSummary.printSummary(history, movieToTracksMap, tracksAdded);
    }
}
