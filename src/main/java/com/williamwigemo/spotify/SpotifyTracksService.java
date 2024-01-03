package com.williamwigemo.spotify;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.williamwigemo.AppLogging;
import com.williamwigemo.HibernateUtil;
import com.williamwigemo.ImdbSoundtrackResult.Collaborators;
import com.williamwigemo.entities.SpotifyTrackEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class SpotifyTracksService {

    private final SpotifyAPI spotifyAPI;
    private final Logger logger = AppLogging.buildLogger(SpotifyTracksService.class);

    public SpotifyTracksService(SpotifyAPI spotifyAPI) {
        this.spotifyAPI = spotifyAPI;
    }

    public SpotifyTrackEntity findTrack(String trackName, Collaborators collabs) {
        if (collabs.getPrimaryCollaborator() == null) {
            return null;
        }

        SpotifyTrackEntity trackEntity = getTrackFromDb(trackName, collabs);

        if (trackEntity != null) {
            if (trackEntity.getSpotifyUri() != null) {
                logger.fine("Found existing Spotify URI for " + trackName);
                return trackEntity;
            }

            logger.fine(trackName + " already exists in database, but does not exist on Spotify");
            return null;
        }

        // else get from database and add it to database
        try {
            Optional<SpotifyTrack> track = this.spotifyAPI
                    .getTrackByName(trackName, collabs.getPrimaryCollaborator());

            if (track.isPresent()) {
                logger.fine(String.format("Found similar track to \"%s\" on Spotify (%s)", trackName,
                        track.get().getUri()));
                return addTrack(track.get().toEntity());
            } else {
                logger.fine(String.format("Could not find \"%s\" on Spotify", trackName));
                SpotifyTrackEntity entity = new SpotifyTrackEntity(trackName, collabs.toList());
                addTrack(entity);
                return null;
            }
        } catch (SpotifyApiException e) {
            logger.fine(String.format("An error occurred when searchinf for \"%s\" on Spotify", trackName));
            return null;
        }
    }

    private SpotifyTrackEntity getSpotifyTrackByUri(String spotifyUri) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<SpotifyTrackEntity> cr = cb.createQuery(SpotifyTrackEntity.class);
        Root<SpotifyTrackEntity> root = cr.from(SpotifyTrackEntity.class);

        cr.select(root).where(cb.equal(root.get("spotifyUri"), spotifyUri));

        Query<SpotifyTrackEntity> query = session.createQuery(cr);
        query.setMaxResults(1);
        List<SpotifyTrackEntity> results = query.getResultList();

        transaction.commit();
        session.close();

        return results.isEmpty() ? null : results.get(0);
    }

    private SpotifyTrackEntity addTrack(SpotifyTrackEntity entity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        SpotifyTrackEntity existing = getSpotifyTrackByUri(entity.getSpotifyUri());
        if (existing == null) {
            session.persist(entity);
        } else {
            entity = existing;
        }

        transaction.commit();
        session.close();

        return entity;
    }

    private SpotifyTrackEntity getTrackFromDb(String trackName, Collaborators collaborators) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<SpotifyTrackEntity> cr = cb.createQuery(SpotifyTrackEntity.class);
        Root<SpotifyTrackEntity> root = cr.from(SpotifyTrackEntity.class);

        cr.select(root)
                .where(cb.equal(root.get("trackName"), trackName))
                .where(cb.isMember(collaborators.getPrimaryCollaborator(), root.get("collaborators")));

        Query<SpotifyTrackEntity> query = session.createQuery(cr);
        List<SpotifyTrackEntity> results = query.getResultList();

        transaction.commit();

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }
}
