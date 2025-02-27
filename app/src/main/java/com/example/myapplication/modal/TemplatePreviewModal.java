package com.example.myapplication.modal;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class TemplatePreviewModal extends DialogFragment {

    private static final String ARG_TEMPLATE_URI = "template_uri";

    public static TemplatePreviewModal newInstance(Uri templateUri) {
        TemplatePreviewModal modal = new TemplatePreviewModal();
        Bundle args = new Bundle();
        args.putString(ARG_TEMPLATE_URI, templateUri.toString());
        modal.setArguments(args);
        return modal;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.preview_tree_layout, null);
        LinearLayout previewContainer = view.findViewById(R.id.preview_container);

        createPreviewTreeLayout(previewContainer, 200);

        String templateUriString = getArguments().getString(ARG_TEMPLATE_URI);
        Uri templateUri = Uri.parse(templateUriString);
        try {
            InputStream is = requireActivity().getContentResolver().openInputStream(templateUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject templateData = new JSONObject(sb.toString());
            if (templateData.has("dotColors")) {
                applyDotColorsToPreview(previewContainer, templateData.getJSONObject("dotColors"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Template Preview")
                .setView(view)
                .setPositiveButton("OK", null);
        return builder.create();
    }

    private void createPreviewTreeLayout(LinearLayout container, int numDots) {
        int dotCount = 0;
        int rowNumber = 1;
        while (dotCount < numDots) {
            LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER);
            for (int i = 0; i < rowNumber && dotCount < numDots; i++) {
                View dot = new View(getActivity());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
                params.setMargins(2, 2, 2, 2);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.circle_shape);
                dot.setTag(dotCount);
                row.addView(dot);
                dotCount++;
            }
            container.addView(row);
            rowNumber++;
        }
    }

    private void applyDotColorsToPreview(LinearLayout container, JSONObject dotColors) {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) container.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                View dot = row.getChildAt(j);
                Object tag = dot.getTag();
                if (tag != null) {
                    int dotIndex = (Integer) tag;
                    if (dotColors.has(String.valueOf(dotIndex))) {
                        try {
                            String colorStr = dotColors.getString(String.valueOf(dotIndex));
                            dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStr)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}