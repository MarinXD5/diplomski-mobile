package com.example.myapplication.utils.Adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class TemplateViewHolder extends RecyclerView.ViewHolder {
    public TextView fileNameText;
    public TemplateViewHolder(@NonNull View itemView) {
        super(itemView);
        fileNameText = itemView.findViewById(R.id.templateFileName);
    }
}
