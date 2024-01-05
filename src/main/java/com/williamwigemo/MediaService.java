package com.williamwigemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.williamwigemo.entities.MediaEntity;
import com.williamwigemo.entities.SpotifyTrackEntity;
import com.williamwigemo.spotify.SpotifyAPI;
import com.williamwigemo.spotify.SpotifyTracksService;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class MediaService {

    private static final int MsToNextSoundtrackFetch = 7 * 24 * 60 * 60 * 1000;
    private final ImdbSoundtrackFetcher soundtrackFetcher;
    private final SpotifyTracksService spotifyTracksService;
    private final Logger logger = AppLogging.buildLogger(MediaService.class);

    public MediaService(SpotifyAPI spotifyAPI) {
        this.soundtrackFetcher = new ImdbSoundtrackFetcher();
        this.spotifyTracksService = new SpotifyTracksService(spotifyAPI);
    }

    public MediaEntity getMediaByImdbId(String imdbId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MediaEntity> cr = cb.createQuery(MediaEntity.class);
        Root<MediaEntity> root = cr.from(MediaEntity.class);
        cr.select(root)
                .where(cb.equal(root.get("imdbId"), imdbId));

        Query<MediaEntity> query = session.createQuery(cr);
        List<MediaEntity> results = query.getResultList();

        transaction.commit();
        session.close();

        return results.isEmpty() ? null : results.get(0);
    }

    public MediaEntity fetchMedia(MediaEntity entity) {

        MediaEntity mediaEntity = getMediaByImdbId(entity.getImdbId());
        if (mediaEntity == null) {
            // then create it
            logger.fine(String.format("Media \"%s\" was not in database, creating new media", entity.getTitle()));
            mediaEntity = createMedia(entity);
        }

        if (shouldFetchSoundtracks(mediaEntity)) {
            try {
                // logger.fine(String.format("Fetching soundtracks from %s...",
                // entity.toString()));
                getSoundtracks(mediaEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.fine(String.format("Soundtracks from %s was recently fetched. Skipping.", entity.toString()));
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        mediaEntity.setSoundtracks(new HashSet<>(session.get(MediaEntity.class, mediaEntity.getId()).getSoundtracks()));

        mediaEntity.getSoundtracks().forEach(o -> o.setCollaborators(new ArrayList<>(o.getCollaborators())));

        transaction.commit();
        session.close();

        return mediaEntity;
    }

    private MediaEntity createMedia(MediaEntity entity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(entity);

        transaction.commit();
        session.close();

        return entity;
    }

    public boolean shouldFetchSoundtracks(MediaEntity entity) {
        if (entity == null)
            return true;
        return System.currentTimeMillis() > entity.getLastFetchedSoundtracks() + MsToNextSoundtrackFetch;
    }

    public void getSoundtracks(MediaEntity media) throws IOException {
        for (ImdbSoundtrackResult res : this.soundtrackFetcher.getSoundtracks(media.getImdbId())) {
            SpotifyTrackEntity trackEntity = this.spotifyTracksService.findTrack(res.getTitle(),
                    res.getCollaborators());

            if (trackEntity != null) {
                addTrackToEntity(media, trackEntity);
            }
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        MediaEntity mediaEntity = session.get(MediaEntity.class, media.getId());

        mediaEntity.setLastFetchedSoundtracks(System.currentTimeMillis());

        transaction.commit();
        session.close();

    }

    private void addTrackToEntity(MediaEntity media, SpotifyTrackEntity trackEntity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        MediaEntity mediaEntity = session.get(MediaEntity.class, media.getId());
        trackEntity = session.merge(trackEntity);

        mediaEntity.getSoundtracks().add(trackEntity);
        mediaEntity.setLastFetchedSoundtracks(System.currentTimeMillis());

        trackEntity.getMedias().add(mediaEntity);

        session.persist(mediaEntity);
        session.persist(trackEntity);

        transaction.commit();
        session.close();
    }
}
