package io.proj3ct.space_monitoring_bot.model;

public class Nasa {
    private String explanation;
    private String copyright;
    private String date;
    private String url;

    public String getExplanation() {
        return explanation;
    }

    public String getCopyright() {
        return copyright != null ? copyright : "NASA";
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }
}
