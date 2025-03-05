package com.example.myapplication.utils.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.TemplateFileMModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.google.firebase.firestore.FieldValue;

import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ViewHolder> {

    private Context context;
    private List<TemplateFileMModel> templateList;
    private FirebaseFirestore firestore;

    public MarketplaceAdapter(Context context, List<TemplateFileMModel> templateList) {
        this.context = context;
        this.templateList = templateList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_marketplace_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemplateFileMModel template = templateList.get(position);

        holder.templateName.setText(template.getName() != null ? template.getName() : "Unknown");
        holder.templatePrice.setText(template.isPurchased() ? "Purchased" : template.getPrice() + " Kredits");

        if (template.getFileUrl() != null && !template.getFileUrl().isEmpty()) {
            Picasso.get().load(template.getFileUrl()).into(holder.templateImage);
        } else {
            holder.templateImage.setImageResource(R.drawable.default_template_image);
        }

        holder.purchaseButton.setText(template.isPurchased() ? "Download" : "Purchase");
        holder.purchaseButton.setOnClickListener(v -> {
            if (template.isPurchased()) {
                downloadFile(template.getFileUrl());
            } else {
                purchaseTemplate(template);
            }
        });

        holder.likeButton.setImageResource(template.isLiked() ? R.drawable.like_filled : R.drawable.likes);
        holder.likeButton.setOnClickListener(v -> {
            boolean newLiked = !template.isLiked();
            template.setLiked(newLiked);
            notifyItemChanged(position);
            Toast.makeText(context, newLiked ? "Liked!" : "Unliked!", Toast.LENGTH_SHORT).show();

            int likeIncrement = newLiked ? 1 : -1;
            firestore.collection("file_info").document(template.getId()).update("numOfLikes", FieldValue.increment(likeIncrement));

        });

        holder.favoriteButton.setImageResource(template.isFavorited() ? R.drawable.favorite_filled : R.drawable.favorite);
        holder.favoriteButton.setOnClickListener(v -> {
            boolean newFav = !template.isFavorited();
            template.setFavorited(newFav);
            notifyItemChanged(position);
            Toast.makeText(context, newFav ? "Added to Favorites!" : "Removed from Favorites!", Toast.LENGTH_SHORT).show();

            int favIncrement = newFav ? 1 : -1;
            firestore.collection("file_info")
                    .document(template.getId())
                    .update("numOfFavorites", FieldValue.increment(favIncrement));

        });
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView templateImage, likeButton, favoriteButton;
        TextView templateName, templatePrice;
        Button purchaseButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            templateImage = itemView.findViewById(R.id.templateImage);
            templateName = itemView.findViewById(R.id.templateName);
            templatePrice = itemView.findViewById(R.id.templatePrice);
            purchaseButton = itemView.findViewById(R.id.purchaseButton);
            likeButton = itemView.findViewById(R.id.likeButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }

    private void downloadFile(String fileUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
        context.startActivity(intent);
    }

    private void purchaseTemplate(TemplateFileMModel template) {
        Toast.makeText(context, "Purchased " + template.getName() + " for " + template.getPrice() + " Kredits!", Toast.LENGTH_SHORT).show();
    }
}

