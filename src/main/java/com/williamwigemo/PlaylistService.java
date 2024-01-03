package com.williamwigemo;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.williamwigemo.entities.PlaylistEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class PlaylistService {
    public PlaylistEntity getPlaylistByName(String playlistName) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<PlaylistEntity> cr = cb.createQuery(PlaylistEntity.class);
        Root<PlaylistEntity> root = cr.from(PlaylistEntity.class);
        cr.select(root)
                .where(cb.equal(root.get("playlistName"), playlistName));

        Query<PlaylistEntity> query = session.createQuery(cr);
        List<PlaylistEntity> results = query.getResultList();

        transaction.commit();
        session.close();

        return results.isEmpty() ? null : results.get(0);
    }

    public PlaylistEntity createPlaylist(PlaylistEntity playlistEntity) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        session.persist(playlistEntity);

        transaction.commit();
        session.close();

        return playlistEntity;
    }

    public PlaylistEntity createPlaylistIfNotExist(PlaylistEntity playlistEntity) {
        PlaylistEntity existingPlaylist = getPlaylistByName(playlistEntity.getPlaylistName());
        if (existingPlaylist == null) {
            return createPlaylist(playlistEntity);
        }

        return existingPlaylist;
    }
}
