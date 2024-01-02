package com.williamwigemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

    private static final int MillisToNextSoundtrackFetch = 5 * 60 * 60 * 1000;
    private final ImdbSoundtrackFetcher soundtrackFetcher;
    private final SpotifyTracksService spotifyTracksService;

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

        return results.isEmpty() ? null : results.getFirst();
    }

    public MediaEntity fetchMedia(MediaEntity entity) {

        MediaEntity mediaEntity = getMediaByImdbId(entity.getImdbId());
        if (mediaEntity == null) {
            // then create it
            mediaEntity = createMedia(entity);
        }

        if (shouldFetchSoundtracks(mediaEntity)) {
            try {
                getSoundtracks(mediaEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        return System.currentTimeMillis() > entity.getLastFetchedSoundtracks() + MillisToNextSoundtrackFetch;
    }

    public void getSoundtracks(MediaEntity media) throws IOException {
        for (ImdbSoundtrackResult res : this.soundtrackFetcher.getSoundtracks(media.getImdbId())) {
            SpotifyTrackEntity trackEntity = this.spotifyTracksService.getTrackByName(res.title);

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
