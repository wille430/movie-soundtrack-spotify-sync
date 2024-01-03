package com.williamwigemo.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Media", uniqueConstraints = { @UniqueConstraint(columnNames = { "id" }) })
public class MediaEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    public Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "year", nullable = true)
    private int year;

    @Column(name = "imdbId", nullable = false, unique = true)
    private String imdbId;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(name = "Media_SpotifyTrack", joinColumns = { @JoinColumn(name = "media_id") }, inverseJoinColumns = {
            @JoinColumn(name = "spotifyTrack_id") })
    private Set<SpotifyTrackEntity> soundtracks = new HashSet<>();

    @Column(name = "lastFetchedSoundtracks", nullable = true)
    private Long lastFetchedSoundtracks;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public Set<SpotifyTrackEntity> getSoundtracks() {
        return soundtracks;
    }

    public void setSoundtracks(Set<SpotifyTrackEntity> soundtracks) {
        this.soundtracks = soundtracks;
    }

    public Long getLastFetchedSoundtracks() {
        return lastFetchedSoundtracks;
    }

    public void setLastFetchedSoundtracks(Long lastFetchedSoundtracks) {
        this.lastFetchedSoundtracks = lastFetchedSoundtracks;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("\"%s (%s)\" (%s) ", this.title, this.year, this.imdbId);
    }
}
