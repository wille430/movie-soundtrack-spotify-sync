package com.williamwigemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class AppSettings {
    private static final String TRAKT_CLIENT_ID = "TRAKT_CLIENT_ID";
    private static final String TRAKT_CLIENT_SECRET = "TRAKT_CLIENT_SECRET";
    private static final String SPOTIFY_CLIENT_ID = "SPOTIFY_CLIENT_ID";
    private static final String PORT = "PORT";

    private static final int DefaultPort = 3000;

    private static AppSettings appCredentials;

    public String traktClientId;
    public String traktClientSecret;
    public String spotifyClientId;
    public Integer port;

    private static final Logger logger = AppLogging.buildLogger(AppSettings.class);

    public static AppSettings getSettings() {
        if (appCredentials == null) {
            appCredentials = new AppSettings("app.properties");
        }

        return appCredentials;
    }

    private void loadProperties(String filePath) throws IOException {
        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(filePath);
        properties.load(input);

        this.traktClientId = properties.getProperty(TRAKT_CLIENT_ID);
        this.traktClientSecret = properties.getProperty(TRAKT_CLIENT_SECRET);
        this.spotifyClientId = properties.getProperty(SPOTIFY_CLIENT_ID);

        if (properties.getProperty(PORT) != null)
            this.setPort(properties.getProperty(PORT));
    }

    private void loadPropertiesFromEnv() {
        if (System.getenv(TRAKT_CLIENT_ID) != null)
            this.traktClientId = System.getenv(TRAKT_CLIENT_ID);

        if (System.getenv(TRAKT_CLIENT_SECRET) != null)
            this.traktClientSecret = System.getenv(TRAKT_CLIENT_SECRET);

        if (System.getenv(SPOTIFY_CLIENT_ID) != null)
            this.spotifyClientId = System.getenv(SPOTIFY_CLIENT_ID);

        if (System.getenv(PORT) != null)
            this.setPort(System.getenv(PORT));
    }

    private void throwMissingProperty(String property) {
        throw new IllegalArgumentException(String.format(
                "Missing variable \"%s\". It must be provided as environment variable or from property file",
                property));
    }

    private void assertProperties() {
        if (this.traktClientId == null)
            throwMissingProperty(TRAKT_CLIENT_ID);

        if (this.traktClientSecret == null)
            throwMissingProperty(TRAKT_CLIENT_SECRET);

        if (this.spotifyClientId == null)
            throwMissingProperty(SPOTIFY_CLIENT_ID);
    }

    private void assignDefaults() {
        if (this.port == null) {
            logger.info(String.format("No port was specified. Using default port (port %s)", DefaultPort));
            this.port = DefaultPort;
        }
    }

    public AppSettings(String filePath) {
        try {
            loadProperties(filePath);
        } catch (IOException e) {
            logger.info(String.format("%s does not exist. Resolving credentials from environment.", filePath));
            loadPropertiesFromEnv();
        }

        assignDefaults();

        assertProperties();
    }

    public String getTraktClientId() {
        return traktClientId;
    }

    public String getTraktClientSecret() {
        return traktClientSecret;
    }

    public String getSpotifyClientId() {
        return spotifyClientId;
    }

    private void setPort(String port) {
        try {
            this.port = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            logger.warning(String.format("%s is not a valid port", port,
                    DefaultPort));
        }
    }

    public Integer getPort() {
        return port;
    }
}
