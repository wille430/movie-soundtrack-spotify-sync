package com.williamwigemo.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "SpotifyTrack", uniqueConstraints = { @UniqueConstraint(columnNames = { "id" }) })
public class SpotifyTrackEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    public Long id;

    @Column(name = "trackName", nullable = false)
    private String trackName;

    @Column(name = "spotifyUri", nullable = false, unique = true)
    private String spotifyUri;

    @Column(name = "popularity")
    private Integer popularity;

    @ElementCollection
    @CollectionTable(name = "SpotifyTrack_collaborators", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "collaborator")
    @Fetch(FetchMode.SUBSELECT)
    private List<String> collaborators = new ArrayList<>();

    @ManyToMany(mappedBy = "soundtracks")
    private Set<MediaEntity> medias = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getSpotifyUri() {
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public List<String> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(List<String> collaborators) {
        this.collaborators = collaborators;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public Set<MediaEntity> getMedias() {
        return medias;
    }

    public void setMedias(Set<MediaEntity> medias) {
        this.medias = medias;
    }

    @Override
    public int hashCode() {
        return this.spotifyUri.hashCode();
    }
}
