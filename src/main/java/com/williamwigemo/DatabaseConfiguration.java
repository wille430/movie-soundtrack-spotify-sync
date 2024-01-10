package com.williamwigemo;

import java.io.File;

public class DatabaseConfiguration {
    public static void configureDatabase() {
        String dir = AppSettings.getSettings().getDataDir();
        if (dir == null) {
            System.setProperty("database.url", "jdbc:sqlite:./database.db");
        } else {
            System.setProperty("database.url", String.format("jdbc:sqlite:%s/database.db", dir));

            new File(dir).mkdirs();
        }
    }
}
