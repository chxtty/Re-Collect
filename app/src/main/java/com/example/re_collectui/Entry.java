package com.example.re_collectui;


import java.util.Date;

public class Entry {
    private String title;
    private String date;
    private int entryId;
    private int author;
    private String content;


    public Entry(String title, String date, int entryId, int author, String content) {
        this.title = title;
        this.date = date;
        this.entryId = entryId;
        this.author = author;
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public int getEntryId() {
        return entryId;
    }

    public int getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
