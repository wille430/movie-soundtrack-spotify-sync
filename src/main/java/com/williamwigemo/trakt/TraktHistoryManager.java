package com.williamwigemo.trakt;

import java.text.ParseException;
import java.util.Date;
import java.util.prefs.Preferences;

import com.williamwigemo.UrlUtils;

public class TraktHistoryManager {
    private static final String LAST_MOVIE_SYNC_KEY = "last_movie_sync";
    private static final TraktHistoryManager TraktHistoryManager = new TraktHistoryManager();
    private static final Preferences Prefs = Preferences.userNodeForPackage(TraktHistoryManager.class);

    public static TraktHistoryManager getInstance() {
        return TraktHistoryManager;
    }

    public Date getLastMovieSync() {
        String dateStr = Prefs.get(LAST_MOVIE_SYNC_KEY, null);
        if (dateStr == null) {
            return null;
        }

        try {
            return UrlUtils.parseISO8601Date(dateStr);
        } catch (ParseException e) {
            System.out.println("Could not parse " + dateStr + " to Date as ISO date");
            e.printStackTrace();
            return null;
        }
    }

    public void setLastMovieSync(String dateStr) {
        Prefs.put(LAST_MOVIE_SYNC_KEY, dateStr);
    }

    public void setLastMovieSync(Date date) {
        setLastMovieSync(UrlUtils.getISO8601Date(date));
    }

    public Preferences getPreferences() {
        return Prefs;
    }
}
