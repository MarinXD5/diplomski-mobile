package com.example.myapplication.utils.Adapters;

import android.net.Uri;

public class TemplateItem {

    private final String displayName;
    private final Uri fileUri;

    public TemplateItem(String displayName, Uri fileUri) {
        this.displayName = displayName;
        this.fileUri = fileUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Uri getFileUri() {
        return fileUri;
    }
}
