package com.williamwigemo;

import java.util.Set;

public interface TrackMetrics<T> {
    public double getSignificanceScore(T track);
}
