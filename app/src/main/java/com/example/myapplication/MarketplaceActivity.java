package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MarketplaceActivity extends BaseActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri selectedFileUri;
    private String selectedFileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.marketplace_activity);

        FloatingActionButton uploadFab = findViewById(R.id.uploadFileFab);
        uploadFab.setOnClickListener(view -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                selectedFileName = getFileName(selectedFileUri);
                showPriceInputDialog();
            }
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "Unknown";
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }


    private void showPriceInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Price for " + selectedFileName);

        final EditText input = new EditText(this);
        input.setHint("Enter price in Kredits");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String price = input.getText().toString().trim();
            if (!price.isEmpty()) {
                confirmUpload(selectedFileName, price);
            } else {
                Toast.makeText(this, "Price cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmUpload(String fileName, String price) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Upload");
        builder.setMessage("Are you sure you want to upload " + fileName + " to the marketplace for " + price + " Kredits?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            System.out.println("Would've saved to marketplace. TEST");
            Toast.makeText(this, "File uploaded!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}