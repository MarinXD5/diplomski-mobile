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

import com.example.myapplication.modal.TemplatePreviewModal;
import com.example.myapplication.utils.Adapters.TemplateAdapter;
import com.example.myapplication.utils.Adapters.TemplateItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TemplatesActivity extends BaseActivity {
    private List<TemplateItem> templateItems = new ArrayList<>();

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
        TemplateAdapter adapter = new TemplateAdapter(templateItems, file -> {
            TemplatePreviewModal.newInstance(file.getFileUri()).show(getSupportFragmentManager(), "preview");

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("templateFilePath", file.getFileUri());
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
        templateItems.clear();

        if (path.startsWith("content://")) {
            Uri treeUri = Uri.parse(path);
            DocumentFile directory = DocumentFile.fromTreeUri(this, treeUri);
            if (directory != null && directory.exists() && directory.isDirectory()) {
                for (DocumentFile docFile : directory.listFiles()) {
                    if (docFile.getName() != null && docFile.getName().endsWith(".mj")) {
                        templateItems.add(new TemplateItem(docFile.getName(), docFile.getUri()));
                    }
                }
            }
        } else {
            File directory = new File(path);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".mj"));
                if (files != null) {
                    for (File f : files) {
                        templateItems.add(new TemplateItem(f.getName(), Uri.fromFile(f)));
                    }
                }
            }
        }
        System.out.println("Loaded templates: " + templateItems);
    }

}
