package com.example.myapplication;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.utils.Adapters.TemplateAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TemplatesActivity extends BaseActivity {
    private final List<File> templateFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.templates_activity);

        RecyclerView recyclerView = findViewById(R.id.templateRecyclerView);
        if (recyclerView == null) {
            Toast.makeText(this, "You don't have any templates to show", LENGTH_SHORT).show();
            throw new NullPointerException("RecyclerView is null.");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadTemplateFiles();
        TemplateAdapter adapter = new TemplateAdapter(templateFiles, file -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("templateFilePath", file.getAbsolutePath());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadTemplateFiles() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String path = prefs.getString("pref_save_location", null);
        if (path == null) {
            path = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath() + "/MyAppTemplates";
        }

        templateFiles.clear();

        if (path.startsWith("content://")) {
            Uri treeUri = Uri.parse(path);
            DocumentFile directory = DocumentFile.fromTreeUri(this, treeUri);
            if (directory != null && directory.exists() && directory.isDirectory()) {
                DocumentFile[] files = directory.listFiles();
                for (DocumentFile docFile : files) {
                    if (docFile.getName() != null && docFile.getName().endsWith(".mj")) {
                        templateFiles.add(new File(docFile.getUri().toString()));
                    }
                }
            }
        } else {
            File directory = new File(path);
            System.out.println("Exists: " + directory.exists()); //DEBUG
            System.out.println("Is directory: " + directory.isDirectory()); //DEBUG
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".mj"));
                if (files != null) {
                    templateFiles.addAll(Arrays.asList(files));
                }
            }
        }
        System.out.println(templateFiles); //DEBUG
    }

}
