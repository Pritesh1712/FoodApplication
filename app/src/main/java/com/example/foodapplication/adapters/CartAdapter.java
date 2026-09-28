package com.example.foodapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.R;
import com.example.foodapplication.models.CartItem;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItemList;
    private final OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChange(CartItem cartItem, int newQuantity);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, OnQuantityChangeListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.tvFoodName.setText(item.getFood().getName());
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getTotalPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuantityChange(item, item.getQuantity() + 1);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuantityChange(item, item.getQuantity() - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvTotalPrice, tvQuantity;
        Button btnMinus, btnPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
