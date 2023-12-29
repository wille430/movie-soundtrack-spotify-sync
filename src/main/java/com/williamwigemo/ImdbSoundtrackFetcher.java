package com.williamwigemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ImdbSoundtrackFetcher {
    private String getUrl(String imdbId) {
        return "https://www.imdb.com/title/" + imdbId + "/soundtrack";
    }

    public List<ImdbSoundtrackResult> getSoundtracks(String imdbId) throws IOException {
        List<ImdbSoundtrackResult> soundtracks = new ArrayList<>();

        Document doc = Jsoup.connect(getUrl(imdbId)).get();

        Elements stEles = doc.select("ul>li[data-testid=\"list-item\"]");

        // extract title
        for (Element ele : stEles) {
            Element titleEle = ele.select("span").first();
            assert titleEle != null;

            ImdbSoundtrackResult st = new ImdbSoundtrackResult(titleEle.text());
            soundtracks.add(st);
        }

        return soundtracks;
    }
}
