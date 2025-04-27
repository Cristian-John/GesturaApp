package com.example.gesturaapp;

public class LearnCategory {
    private String title;
    private int imageResId;

    public LearnCategory(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}
