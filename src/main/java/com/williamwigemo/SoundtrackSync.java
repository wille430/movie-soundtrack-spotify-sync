package com.williamwigemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.williamwigemo.entities.MediaEntity;
import com.williamwigemo.entities.SpotifyTrackEntity;
import com.williamwigemo.spotify.SpotifyAPI;
import com.williamwigemo.spotify.SpotifyApiException;
import com.williamwigemo.spotify.SpotifyPlaylistSync;
import com.williamwigemo.trakt.TraktApi;
import com.williamwigemo.trakt.TraktApiException;
import com.williamwigemo.trakt.TraktHistoryManager;

public class SoundtrackSync {
    private final SpotifyAPI spotifyAPI;
    private final TraktApi traktApi;
    private final MediaService mediaService;
    private final TraktHistoryManager historyManager;
    private final Logger logger;

    public SoundtrackSync() throws IOException {
        this.spotifyAPI = new SpotifyAPI();
        this.traktApi = new TraktApi();
        this.mediaService = new MediaService(this.spotifyAPI);
        this.historyManager = TraktHistoryManager.getInstance();
        this.logger = AppLogging.buildLogger(SoundtrackSync.class);
    }

    private Map<MediaEntity, Set<SpotifyTrackEntity>> loadSoundtracks(List<MediaEntity> history,
            Set<String> soundtrackNames)
            throws IOException, SpotifyApiException {

        Map<MediaEntity, Set<SpotifyTrackEntity>> imdbIdToTracks = new HashMap<>();
        for (MediaEntity entity : history) {
            entity = this.mediaService.fetchMedia(entity);
            imdbIdToTracks.put(entity, entity.getSoundtracks());
        }

        return imdbIdToTracks;
    }

    public void syncFromDate(Date fromDate) {
        Date lastMovieSync = new Date();

        try {
            this.traktApi.authenticate();
        } catch (TraktApiException | IOException e) {
            logger.severe("Failed to authenticate with Trakt.");
            e.printStackTrace();
            return;
        }
        try {
            this.spotifyAPI.authenticate();
        } catch (SpotifyApiException | IOException e) {
            logger.severe("Failed to authenticate with Spotify.");
            e.printStackTrace();
            return;
        }

        // load watched movies
        List<MediaEntity> history = new ArrayList<>();
        try {
            history.addAll(this.traktApi.getWatchedMovies(fromDate).stream().map(o -> o.toEntity()).toList());
            history.addAll(this.traktApi.getWatchedShows(fromDate).stream().map(o -> o.getShow().toEntity()).toList());
        } catch (TraktApiException e) {
            logger.severe("Failed to load shows and movie history from Trakt.");
            e.printStackTrace();
            return;
        }

        if (history.isEmpty()) {
            logger.info("No new movies or shows were found. Nothing to do.");
            return;
        }

        // load soundtracks to respective movie
        Set<String> soundtrackNames = new HashSet<>();

        Map<MediaEntity, Set<SpotifyTrackEntity>> movieToTracksMap;
        try {
            movieToTracksMap = loadSoundtracks(history, soundtrackNames);
        } catch (SpotifyApiException | IOException e) {
            logger.severe("Failed to load soundtracks from Spotify.");
            e.printStackTrace();
            return;
        }

        // create/update spotify playlist
        SpotifyPlaylistSync playlistSync = new SpotifyPlaylistSync(spotifyAPI);

        logger.info("Populating playlist with new soundtracks...");
        Set<SpotifyTrackEntity> tracksAdded;
        try {
            tracksAdded = playlistSync.updateMovies(movieToTracksMap);
        } catch (SpotifyApiException e) {
            logger.severe("Failed adding soundtracks to Spotify playlist.");
            e.printStackTrace();
            return;
        }

        this.historyManager.setLastMovieSync(lastMovieSync);

        SyncSummary.printSummary(history, movieToTracksMap, tracksAdded);
    }

    public void syncNew() {
        Date fromDate = this.historyManager.getLastMovieSync();
        if (fromDate != null) {
            logger.info(String.format("Syncing... (Last sync: %s)", UrlUtils.getISO8601Date(fromDate)));
        } else {
            logger.info("Syncing... (Last sync: never)");
        }
        syncFromDate(fromDate);
    }

    public void syncAll() {
        syncFromDate(null);
    }
}
