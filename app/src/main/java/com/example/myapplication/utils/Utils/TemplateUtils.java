package com.example.myapplication.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Objects;

public class TemplateUtils {
    public static boolean saveTemplate(Context context, String templateName, JSONObject templateData) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String path = prefs.getString("pref_save_location", null);

        if (path == null) {
            path = Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath() + "/MyAppTemplates";
        }

        if (path.startsWith("content://")) {
            Uri treeUri = Uri.parse(path);
            DocumentFile pickedDir = DocumentFile.fromTreeUri(context, treeUri);
            if (pickedDir == null) {
                return false;
            }
            String fileName = templateName + ".mj";
            DocumentFile existingFile = pickedDir.findFile(fileName);
            if (existingFile != null) {
                existingFile.delete();
            }
            DocumentFile newFile = pickedDir.createFile("application/octet-stream", fileName);
            if (newFile == null) {
                return false;
            }
            try (OutputStream os = context.getContentResolver().openOutputStream(newFile.getUri())) {
                if (os == null) return false;
                os.write(templateData.toString(2).getBytes());
                os.flush();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File directory = new File(path);
            if (!directory.exists()) {
                boolean success = directory.mkdirs();
                if (!success) {
                    return false;
                }
            }
            String filename = templateName + ".mj";
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

}
