package com.example.gesturaapp;

public class QuizResult {
    private int id;
    private String type;     // "Quiz" or "Replication"
    private String subject;  // ✅ e.g. "Alphabet", "Numbers"
    private int score;
    private int total;
    private String date;     // dateTaken

    public QuizResult() {}

    // ✅ Full constructor used by ZYQuizDatabaseHelper.getAllResults()
    public QuizResult(int id, String type, String subject, int score, int total, String date) {
        this.id = id;
        this.type = type;
        this.subject = subject;
        this.score = score;
        this.total = total;
        this.date = date;
    }

    // ✅ Convenience constructor
    public QuizResult(String type, String subject, int score, int total, String date) {
        this.type = type;
        this.subject = subject;
        this.score = score;
        this.total = total;
        this.date = date;
    }

    // ----- Getters -----
    public int getId() { return id; }
    public String getType() { return type; }
    public String getSubject() { return subject; } // ✅ Added
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public String getDate() { return date; }

    // ----- Setters -----
    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setSubject(String subject) { this.subject = subject; } // ✅ Added
    public void setScore(int score) { this.score = score; }
    public void setTotal(int total) { this.total = total; }
    public void setDate(String date) { this.date = date; }

    // ----- Backwards compatibility -----
    public String getCategory() { return type; }
    public void setCategory(String category) { this.type = category; }
    public String getDateTaken() { return date; }
    public void setDateTaken(String dateTaken) { this.date = dateTaken; }
}
