package com.williamwigemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Logger;

public class AppSettings {
    private static final String TRAKT_CLIENT_ID = "TRAKT_CLIENT_ID";
    private static final String TRAKT_CLIENT_SECRET = "TRAKT_CLIENT_SECRET";
    private static final String SPOTIFY_CLIENT_ID = "SPOTIFY_CLIENT_ID";
    private static final String SPOTIFY_CLIENT_SECRET = "SPOTIFY_CLIENT_SECRET";
    private static final String PORT = "PORT";
    private static final String APP_URL = "APP_URL";
    private static final String DATA_DIRECTORY = "DATA_DIRECTORY";

    private static final int DefaultPort = 3000;
    private static final String DefaultHostname = "localhost";

    private static AppSettings appCredentials;

    public String traktClientId;
    public String traktClientSecret;
    public String spotifyClientId;
    public String spotifyClientSecret;
    private Integer port;
    private String appUrl;
    private String dataDir;

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
        this.spotifyClientSecret = properties.getProperty(SPOTIFY_CLIENT_SECRET);
        this.setAppUrl(properties.getProperty(APP_URL));

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

        if (System.getenv(SPOTIFY_CLIENT_SECRET) != null)
            this.spotifyClientSecret = System.getenv(SPOTIFY_CLIENT_SECRET);

        if (System.getenv(PORT) != null)
            this.setPort(System.getenv(PORT));

        if (System.getenv(APP_URL) != null)
            this.setAppUrl(System.getenv(APP_URL));

        if (System.getenv(DATA_DIRECTORY) != null) {
            String dir = System.getenv(DATA_DIRECTORY);
            logger.info(String.format("Storing application data in directory %s", dir));
            this.dataDir = dir;
        }
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

        if (this.spotifyClientSecret == null)
            throwMissingProperty(SPOTIFY_CLIENT_SECRET);
    }

    private void assignDefaults() {
        if (this.port == null) {
            logger.fine(String.format("No port was specified. Using default port (port %s)", DefaultPort));
            this.port = DefaultPort;
        }

        if (this.appUrl == null)
            this.appUrl = String.format("http://%s:%s", DefaultHostname, DefaultPort);
    }

    public AppSettings(String filePath) {
        try {
            loadProperties(filePath);
        } catch (IOException e) {
            // do nothing
        }

        loadPropertiesFromEnv();

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

    public String getAppUrl() {
        return appUrl;
    }

    private void setAppUrl(String url) {
        if (url == null)
            return;

        try {
            new URI(url);
            this.appUrl = url;
        } catch (URISyntaxException e) {
            logger.warning(String.format("%s is not a valid url", url));
        }
    }

    public String getSpotifyClientSecret() {
        return spotifyClientSecret;
    }

    public String getDataDir() {
        return this.dataDir;
    }
}
