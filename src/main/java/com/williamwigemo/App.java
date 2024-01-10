package com.williamwigemo;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class App {

    private static final Logger logger = AppLogging.buildLogger(App.class);

    public static void main(String[] args) throws IOException {
        DatabaseConfiguration.configureDatabase();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        SoundtrackSync soundtrackSync = new SoundtrackSync();

        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Syncing newly watched movies and shows...");
            soundtrackSync.syncNew();
            logger.info("Sync finished");
        }, 0, 5, TimeUnit.MINUTES);
    }
}
