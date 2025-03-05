package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.FileInfoModel;
import com.example.myapplication.utils.Adapters.MarketplaceAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.myapplication.models.TemplateFileMModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class MarketplaceActivity extends BaseActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private String selectedFileName;
    private Uri selectedFileUri;
    private FirebaseStorage storage;
    private List<TemplateFileMModel> templateList;
    private MarketplaceAdapter adapter;
    private RecyclerView recyclerView;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.marketplace_activity);

        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        templateList = new ArrayList<>();

        recyclerView = findViewById(R.id.marketplaceRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MarketplaceAdapter(this, templateList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton uploadFab = findViewById(R.id.uploadFileFab);
        uploadFab.setOnClickListener(view -> openFilePicker());
        listFiles();
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
                confirmUpload(selectedFileName, Integer.parseInt(price));
            } else {
                Toast.makeText(this, "Price cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmUpload(String fileName, int price) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Upload");
        builder.setMessage("Are you sure you want to upload " + fileName + " to the marketplace for " + price + " Kredits?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            uploadFile(selectedFileUri, fileName, price);
            Toast.makeText(this, "File uploaded!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void uploadFile(Uri fileUri, String fileName, int price) {
        if (fileUri != null) {
            if (fileName.endsWith(".mj")) {
                fileName = fileName.substring(0, fileName.lastIndexOf(".mj"));
            }

            String finalFileName = fileName + "_" + System.currentTimeMillis() + ".mj";

            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("uploads/" + finalFileName);

            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        templateList.add(new TemplateFileMModel(finalFileName, downloadUrl, price, false, false, false));
                        adapter.notifyDataSetChanged();
                    }))
                    .addOnFailureListener(e -> System.out.println("Upload failed: " + e.getMessage()));

            FileInfoModel fileInfoModel = new FileInfoModel(fileName + ".mj", Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName(), price);

            firestore.collection("file_info").document(finalFileName).set(fileInfoModel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            System.out.println("Saved to DB");
                        } else {
                            System.out.println("Error");
                        }
                    });
        }
    }

    private void listFiles() {
        StorageReference storageRef = storage.getReference().child("uploads");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            templateList.clear();
            for (StorageReference item : listResult.getItems()) {
                firestore.collection("file_info").document(item.getName())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            int price;
                            if (documentSnapshot.exists()) {
                                TemplateFileMModel model = documentSnapshot.toObject(TemplateFileMModel.class);
                                if (model != null) {
                                    price = model.getPrice();
                                } else {
                                    price = 0;
                                }
                            } else {
                                price = 0;
                            }
                            item.getDownloadUrl().addOnSuccessListener(uri -> {
                                TemplateFileMModel newTemplate = new TemplateFileMModel(
                                        item.getName(),
                                        item.getName(),
                                        uri.toString(),
                                        price,
                                        false,
                                        false,
                                        false
                                );
                                templateList.add(newTemplate);
                                adapter.notifyDataSetChanged();
                            }).addOnFailureListener(e ->
                                    System.out.println("Error getting download URL: " + e.getMessage())
                            );
                        }).addOnFailureListener(e ->
                                System.out.println("Error fetching document: " + e.getMessage())
                        );
            }
        }).addOnFailureListener(e ->
                System.out.println("⚠️ Error listing files: " + e.getMessage())
        );
    }

}
