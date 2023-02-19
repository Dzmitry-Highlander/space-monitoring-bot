package io.proj3ct.space_monitoring_bot.service;

import com.google.gson.Gson;
import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.space_monitoring_bot.model.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ParserService {
    private final URL url = new URL("https://fdo.rocketlaunch.live/json/launches/next/5");

    public ParserService() throws MalformedURLException {
    }

    private String getJson() throws IOException {
        String json = IOUtils.toString(url, StandardCharsets.UTF_8);

        return new JSONObject(json).toString();
    }

    public String parser() throws IOException {
        StringBuilder launchTimeTable = new StringBuilder();
        String json = getJson();

        Gson gson = new Gson();
        Page page = gson.fromJson(json, Page.class);

        for (Result result : page.getResult()) {
            Pad pad = result.getPad();
            Location location = pad.getLocation();
            String emoji = EmojiParser.parseToUnicode(":rocket: ");

            launchTimeTable.append(emoji).append(result.getLaunch_description()).append("\n")
                    .append(location.getName()).append(", ").append(location.getCountry()).append("\n\n");
        }

        return launchTimeTable.toString();
    }
}
