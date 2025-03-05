package com.example.myapplication.models;


import java.util.HashMap;
import java.util.Map;

public class FileInfoModel {
    private String fileName;
    private String author;
    private int price;
    private int numOfLikes;
    private int numOfFavorites;

    public FileInfoModel() {}

    public FileInfoModel(String fileName, String author, int price) {
        this.fileName = fileName;
        this.author = author;
        this.price = price;
        this.numOfLikes = 0;
        this.numOfFavorites = 0;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getNumOfLikes() { return numOfLikes; }
    public void setNumOfLikes(int numOfLikes) { this.numOfLikes = numOfLikes; }

    public int getNumOfFavorites() { return numOfFavorites; }
    public void setNumOfFavorites(int numOfFavorites) { this.numOfFavorites = numOfFavorites; }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("fileName", fileName);
        data.put("author", author);
        data.put("price", price);
        data.put("numOfLikes", numOfLikes);
        data.put("numOfFavorites", numOfFavorites);
        return data;
    }
}

