package com.example.gesturaapp;

public class LessonItem {
    private String title;
    private String videoFileName;  // "a" instead of "a.mp4"

    public LessonItem(String title, String videoFileName) {
        this.title = title;
        this.videoFileName = videoFileName;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoFileName() {
        return videoFileName;
    }
}

