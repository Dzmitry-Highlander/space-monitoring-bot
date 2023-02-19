package io.proj3ct.space_monitoring_bot.service;

import com.google.gson.Gson;
import io.proj3ct.space_monitoring_bot.model.Nasa;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NasaParserService {
    private final URL url =
            new URL("https://api.nasa.gov/planetary/apod?api_key=UJVyiK2hrI4aNOyqGah0pFyoByVmeKfrPVngerKe");

    public NasaParserService() throws MalformedURLException {
    }


    private String getJson() throws IOException {
        String json = IOUtils.toString(url, StandardCharsets.UTF_8);

        return new JSONObject(json).toString();
    }

    private Nasa init() throws IOException {
        String json = getJson();
        Gson gson = new Gson();

        return gson.fromJson(json, Nasa.class);
    }

    public String parser() throws IOException {
        StringBuilder nasaPhotoOfTheDay = new StringBuilder();
        Nasa init = init();
        String explanation = init.getExplanation();
        String copyright = init.getCopyright();
        String date = init.getDate();

        nasaPhotoOfTheDay.append(explanation).append("\n\n").append(copyright).append("\n")
                .append(date);

        return nasaPhotoOfTheDay.toString();
    }

    public String photoURL() throws IOException {
        Nasa init = init();

        return init.getUrl();
    }
}
