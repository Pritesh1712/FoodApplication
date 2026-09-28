package com.example.foodapplication.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.foodapplication.R;
import com.example.foodapplication.models.Food;

import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final Context context;
    private final List<Food> foodList;
    private final OnFoodAddClickListener listener;

    public interface OnFoodAddClickListener {
        void onFoodAddClick(Food food);
    }

    public FoodAdapter(Context context, List<Food> foodList, OnFoodAddClickListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);

        holder.tvFoodName.setText(food.getName());
        holder.tvFoodDesc.setText(food.getDescription());
        holder.tvFoodPrice.setText(String.format(Locale.getDefault(), "₹%.2f", food.getPrice()));

        String imgStr = food.getImage();
        if (imgStr != null && !imgStr.trim().isEmpty()) {
            if (imgStr.startsWith("content://") || imgStr.startsWith("file://")) {
                Glide.with(context)
                        .load(Uri.parse(imgStr))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.ivFood);
            } else {
                Glide.with(context)
                        .load(imgStr)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(holder.ivFood);
            }
        } else {
            holder.ivFood.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodAddClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFood;
        TextView tvFoodName, tvFoodDesc, tvFoodPrice;
        Button btnAdd;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFood = itemView.findViewById(R.id.ivFood);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
