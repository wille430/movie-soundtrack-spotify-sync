package com.williamwigemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.williamwigemo.ImdbSoundtrackResult.Collaborators;

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

            ImdbSoundtrackResult st = new ImdbSoundtrackResult(titleEle.text(), getCollaborators(ele));
            soundtracks.add(st);
        }

        return soundtracks;
    }

    private Collaborators getCollaborators(Element ele) {
        Map<String, String> props = new HashMap<>();

        ele.select("div>ul>div a").stream()
                .filter(e -> e.text() != null && !e.text().isEmpty() && e.parent().text().contains("by "))
                .map(o -> new SimpleEntry<>(o.parent().text().split("by ")[0] + "by", o.text()))
                .filter(o -> !props.containsKey(o.getKey()))
                .forEach(o -> props.put(o.getKey(), o.getValue()));

        Collaborators collaborators = new Collaborators();

        collaborators.setComposedBy(props.get("Composed by"));
        collaborators.setPerformedBy(props.get("Performed by"));
        collaborators.setWrittenBy(props.get("Written by"));

        return collaborators;
    }
}
