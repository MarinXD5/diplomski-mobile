package com.example.myapplication.utils.directory;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;

public class TemplateUtils {
    public static boolean saveTemplate(Context context, String templateName, JSONObject templateData) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String path = prefs.getString("pref_save_location", "/storage/emulated/0/MyAppTemplates");

        File directory = new File(path);
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                return false;
            }
        }
        String filename = templateName + ".json";
        File file = new File(directory, filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(templateData.toString(2));
            writer.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
