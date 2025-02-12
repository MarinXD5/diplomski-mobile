package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

public class ColorAdapter extends ArrayAdapter<String> {
    private final String[] colorValues;
    private final LayoutInflater inflater;
    private final int selectedIndex;

    public ColorAdapter(Context context, String[] entries, String[] colorValues, int selectedIndex) {
        super(context, 0, entries);
        this.colorValues = colorValues;
        this.inflater = LayoutInflater.from(context);
        this.selectedIndex = selectedIndex;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_color_preference, parent, false);
        }

        RadioButton radio = convertView.findViewById(R.id.radio);
        TextView textView = convertView.findViewById(R.id.color_name);
        View colorPreview = convertView.findViewById(R.id.color_preview);

        textView.setText(getItem(position));

        try {
            int colorInt = Color.parseColor(colorValues[position]);
            colorPreview.setBackgroundColor(colorInt);
        } catch (Exception e) {
            colorPreview.setBackgroundColor(Color.TRANSPARENT);
        }

        radio.setChecked(position == selectedIndex);

        return convertView;
    }
}
