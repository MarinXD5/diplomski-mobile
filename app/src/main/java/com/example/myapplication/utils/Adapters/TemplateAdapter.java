package com.example.myapplication.utils.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    private final List<TemplateItem> templateFiles;
    private final OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(TemplateItem templateFile);
    }

    public TemplateAdapter(List<TemplateItem> templateFiles, OnTemplateClickListener listener) {
        this.templateFiles = templateFiles;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fileNameText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.templateFileName);
        }
    }

    @NonNull
    @Override
    public TemplateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateAdapter.ViewHolder holder, int position) {
        TemplateItem file = templateFiles.get(position);
        holder.fileNameText.setText(file.getDisplayName());
        holder.itemView.setOnClickListener(v -> listener.onTemplateClick(file));
    }

    @Override
    public int getItemCount() {
        return templateFiles.size();
    }
}