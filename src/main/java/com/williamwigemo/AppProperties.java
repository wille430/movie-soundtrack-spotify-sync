package com.williamwigemo;

public abstract class AppProperties {
    public static <T> AppProperties getProperties(Class<T> cls) {
        AppSettings appSettings = AppSettings.getSettings();
        String baseDir = appSettings.getDataDir();

        if (baseDir == null) {
            return new AppPreferences(cls);
        } else {
            String filePath = String.format("%s/%s.properties", baseDir, cls.getSimpleName());
            PersistentProperties properties = new PersistentProperties(filePath);
            return properties;
        }

    }

    public abstract void put(String key, String value);

    public abstract String get(String key, String defaultValue);

    public abstract void store();

    public abstract void clear();
}
