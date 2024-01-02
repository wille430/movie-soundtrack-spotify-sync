package com.williamwigemo.spotify;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import com.williamwigemo.HibernateUtil;
import com.williamwigemo.ImdbSoundtrackResult.Collaborators;
import com.williamwigemo.entities.SpotifyTrackEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class SpotifyTracksService {

    private final SpotifyAPI spotifyAPI;

    public SpotifyTracksService(SpotifyAPI spotifyAPI) {
        this.spotifyAPI = spotifyAPI;
    }

    public SpotifyTrackEntity getTrackByName(String trackName, Collaborators collaborators) {
        SpotifyTrackEntity trackEntity = getTrackFromDb(trackName, collaborators);

        if (trackEntity != null) {
            return trackEntity;
        }

        // else get from database and add it to database
        Optional<SpotifyTrack> track = null;
        try {
            track = this.spotifyAPI.getTrackByName(trackName, collaborators.getPrimaryCollaborator());
        } catch (SpotifyApiException e) {
            return null;
        }

        if (track.isPresent()) {
            return addTrack(track.get().toEntity());
        } else {
            return null;
        }
    }

    private SpotifyTrackEntity addTrack(SpotifyTrackEntity entity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try {
            session.persist(entity);
        } catch (ConstraintViolationException e) {
            entity = session.byNaturalId(SpotifyTrackEntity.class)
                    .using("spotifyUri", entity.getSpotifyUri())
                    .load();
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

        return results.getFirst();
    }
}
