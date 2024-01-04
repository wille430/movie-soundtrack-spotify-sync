package com.williamwigemo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Deduplicator<T> {
    private Set<T> existingSet;
    private Set<T> inSet;

    public Deduplicator() {
        existingSet = new HashSet<>();
        inSet = new HashSet<>();
    }

    public Set<T> getUnique() {
        Set<T> uniqueSet = new HashSet<>();

        uniqueSet.addAll(inSet);
        uniqueSet.removeAll(existingSet);

        return uniqueSet;
    }

    public void addExistingAll(Collection<T> existingTracks) {
        this.existingSet.addAll(existingTracks);
    }

    public void addExisting(T ele) {
        this.existingSet.add(ele);
    }

    public void addIn(Collection<T> tracksIn) {
        this.inSet.addAll(tracksIn);
    }
}
