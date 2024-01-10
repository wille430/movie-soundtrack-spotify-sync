package com.williamwigemo;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AppPreferences extends AppProperties {

    private Preferences prefs;

    public <T> AppPreferences(Class<T> cls) {
        this.prefs = Preferences.systemNodeForPackage(cls);
    }

    @Override
    public void put(String arg0, String arg1) {
        this.prefs.put(arg0, arg1);
    }

    @Override
    public String get(String key, String defaultValue) {
        return this.prefs.get(key, defaultValue);
    }

    @Override
    public void store() {
    }

    @Override
    public void clear() {
        try {
            this.prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
}