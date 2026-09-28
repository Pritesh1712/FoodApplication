package com.example.foodapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.R;
import com.example.foodapplication.models.Food;

import java.util.List;
import java.util.Locale;

public class ManageFoodAdapter extends RecyclerView.Adapter<ManageFoodAdapter.ManageFoodViewHolder> {

    private final Context context;
    private final List<Food> foodList;
    private final OnFoodActionListener listener;

    public interface OnFoodActionListener {
        void onFoodEditClick(Food food);
        void onFoodDeleteClick(Food food);
    }

    public ManageFoodAdapter(Context context, List<Food> foodList, OnFoodActionListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ManageFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_food, parent, false);
        return new ManageFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageFoodViewHolder holder, int position) {
        Food food = foodList.get(position);

        holder.tvFoodName.setText(food.getName());
        holder.tvFoodDesc.setText(food.getDescription());
        holder.tvFoodPrice.setText(String.format(Locale.getDefault(), "₹%.2f", food.getPrice()));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodEditClick(food);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodDeleteClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    static class ManageFoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvFoodDesc, tvFoodPrice;
        ImageButton btnEdit, btnDelete;

        public ManageFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
