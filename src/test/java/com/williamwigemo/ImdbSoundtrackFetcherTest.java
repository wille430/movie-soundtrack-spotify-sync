package com.williamwigemo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class ImdbSoundtrackFetcherTest {

    private ImdbSoundtrackFetcher imdbSoundtrackFetcher;

    public ImdbSoundtrackFetcherTest() {
        this.imdbSoundtrackFetcher = new ImdbSoundtrackFetcher();
    }

    @Test
    public void shouldReturnSoundtracks() throws IOException {
        List<ImdbSoundtrackResult> soundtracks = imdbSoundtrackFetcher.getSoundtracks("tt0137523");
        assertTrue(soundtracks.size() == 14);
    }
}
