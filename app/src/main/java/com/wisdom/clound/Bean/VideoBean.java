package com.wisdom.clound.Bean;

public class VideoBean {
    private int id;
    private String title;
    private String coverUrl;
    private int playCount;
    private String duration;

    public VideoBean(int id, String title, String coverUrl, int playCount, String duration) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.playCount = playCount;
        this.duration = duration;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public int getPlayCount() { return playCount; }
    public String getDuration() { return duration; }
}
