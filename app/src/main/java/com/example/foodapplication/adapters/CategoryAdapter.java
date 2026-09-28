package com.example.foodapplication.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.foodapplication.R;
import com.example.foodapplication.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categoryList;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        String imageUrl = category.getImage();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageUrl = getFallbackImageUrl(category.getName());
        }

        if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
            Glide.with(context)
                    .load(Uri.parse(imageUrl))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivCategory);
        } else {
            Glide.with(context)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.ivCategory);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    private String getFallbackImageUrl(String categoryName) {
        if (categoryName == null) return "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=200";
        String name = categoryName.toLowerCase();
        if (name.contains("starter")) return "https://images.unsplash.com/photo-1541544741938-0af808871cc0?w=200";
        if (name.contains("main course")) return "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=200";
        if (name.contains("sweet") || name.contains("dessert")) return "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=200";
        if (name.contains("pizza")) return "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=200";
        if (name.contains("burger")) return "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200";
        return "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=200";
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategory;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategory = itemView.findViewById(R.id.ivCategory);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
