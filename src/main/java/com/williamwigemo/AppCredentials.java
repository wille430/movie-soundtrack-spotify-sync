package com.williamwigemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppCredentials {
    public String traktClientId;
    public String traktClientSecret;
    public String spotifyClientId;

    private void loadProperties(String filePath) throws IOException {
        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(filePath);
        properties.load(input);

        this.traktClientId = properties.getProperty("trakt_client_id");
        this.traktClientSecret = properties.getProperty("trakt_client_secret");
        this.spotifyClientId = properties.getProperty("spotify_client_id");
    }

    public AppCredentials(String filePath) throws IOException {
        loadProperties(filePath);
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
}
