package com.example.myapplication.models;

public class TemplateFileMModel {
    private String id;
    private String name;
    private String fileUrl;
    private int price;
    private boolean purchased;
    private boolean liked;
    private boolean favorited;

    public TemplateFileMModel() { }

    public TemplateFileMModel(String id, String name, String fileUrl, int price, boolean purchased, boolean liked, boolean favorited) {
        this.id = id;
        this.name = name;
        this.fileUrl = fileUrl;
        this.price = price;
        this.purchased = purchased;
        this.liked = liked;
        this.favorited = favorited;
    }

    public TemplateFileMModel(String finalFileName, String downloadUrl, int price, boolean b, boolean b1, boolean b2) {
        this.name = finalFileName;
        this.fileUrl = downloadUrl;
        this.price = price;
        this.purchased = b;
        this.liked = b1;
        this.favorited = b2;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public String getFileUrl() { return fileUrl; }
    public int getPrice() { return price; }
    public boolean isPurchased() { return purchased; }
    public boolean isLiked() { return liked; }
    public boolean isFavorited() { return favorited; }

    public void setLiked(boolean liked) { this.liked = liked; }
    public void setFavorited(boolean favorited) { this.favorited = favorited; }
}
