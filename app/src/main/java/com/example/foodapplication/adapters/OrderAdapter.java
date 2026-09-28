package com.example.foodapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapplication.R;
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderItem;
import com.example.foodapplication.utils.Constants;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvRestaurantName.setText(order.getRestaurantName());
        holder.tvOrderStatus.setText(order.getOrderStatus());
        holder.tvOrderDate.setText(order.getCreatedAt());
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));

        StringBuilder summary = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                summary.append(item.getQuantity()).append("x ").append(item.getFoodName()).append(", ");
            }
            if (summary.length() > 2) {
                summary.setLength(summary.length() - 2);
            }
        }
        holder.tvOrderSummary.setText(summary.toString());

        String status = order.getOrderStatus() != null ? order.getOrderStatus() : "";

        if (Constants.STATUS_DELIVERED.equalsIgnoreCase(status) || Constants.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            // Dimmed/Blurred effect for completed or cancelled orders
            holder.itemView.setAlpha(0.50f);
            if (Constants.STATUS_DELIVERED.equalsIgnoreCase(status)) {
                holder.tvOrderStatus.setBackgroundResource(R.color.green_bg);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.green_success));
            } else {
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#33C62828"));
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.red_error));
            }
        } else {
            // Bright active active orders on process
            holder.itemView.setAlpha(1.0f);
            holder.tvOrderStatus.setBackgroundResource(R.color.orange_bg);
            holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.primary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvOrderStatus, tvOrderDate, tvOrderSummary, tvTotalAmount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderSummary = itemView.findViewById(R.id.tvOrderSummary);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }
}
