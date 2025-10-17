package com.example.gesturaapp;

import java.util.List;

public class Question {
    private String questionText;
    private List<String> choices;
    private int correctAnswerIndex;
    private String videoUri;


    public Question(String questionText, List<String> choices, int correctAnswerIndex, String videoUri) {
        this.questionText = questionText;
        this.choices = choices;
        this.correctAnswerIndex = correctAnswerIndex;
        this.videoUri = videoUri;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getChoices() {
        return choices;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getVideoUri() {
        return videoUri;
    }
}
