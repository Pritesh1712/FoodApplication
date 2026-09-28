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
import com.example.foodapplication.models.Order;
import com.example.foodapplication.models.OrderItem;
import com.example.foodapplication.utils.Constants;

import java.util.List;
import java.util.Locale;

public class RestaurantOrderAdapter extends RecyclerView.Adapter<RestaurantOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnStatusUpdateClickListener listener;

    public interface OnStatusUpdateClickListener {
        void onStatusUpdate(Order order, String nextStatus);
    }

    public RestaurantOrderAdapter(Context context, List<Order> orderList, OnStatusUpdateClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_restaurant_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        String customer = order.getCustomerName() != null ? order.getCustomerName() : "Customer Order #" + order.getId();
        holder.tvCustomerName.setText(customer);
        holder.tvStatus.setText(order.getOrderStatus());
        holder.tvTotal.setText(String.format(Locale.getDefault(), "Total: ₹%.2f", order.getTotalAmount()));

        StringBuilder itemsSb = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                itemsSb.append(item.getQuantity()).append("x ").append(item.getFoodName()).append(", ");
            }
            if (itemsSb.length() > 2) {
                itemsSb.setLength(itemsSb.length() - 2);
            }
        }
        holder.tvItems.setText(itemsSb.toString());

        String currentStatus = order.getOrderStatus();
        String nextStatus;
        if (Constants.STATUS_PLACED.equals(currentStatus)) {
            nextStatus = Constants.STATUS_ACCEPTED;
            holder.btnNextStatus.setText("Accept Order");
            holder.btnNextStatus.setVisibility(View.VISIBLE);
        } else if (Constants.STATUS_ACCEPTED.equals(currentStatus)) {
            nextStatus = Constants.STATUS_PREPARING;
            holder.btnNextStatus.setText("Start Preparing");
            holder.btnNextStatus.setVisibility(View.VISIBLE);
        } else if (Constants.STATUS_PREPARING.equals(currentStatus)) {
            nextStatus = Constants.STATUS_READY;
            holder.btnNextStatus.setText("Mark Ready for Pickup");
            holder.btnNextStatus.setVisibility(View.VISIBLE);
        } else {
            nextStatus = null;
            holder.btnNextStatus.setVisibility(View.GONE);
        }

        holder.btnNextStatus.setOnClickListener(v -> {
            if (listener != null && nextStatus != null) {
                listener.onStatusUpdate(order, nextStatus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvStatus, tvItems, tvTotal;
        Button btnNextStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnNextStatus = itemView.findViewById(R.id.btnNextStatus);
        }
    }
}
