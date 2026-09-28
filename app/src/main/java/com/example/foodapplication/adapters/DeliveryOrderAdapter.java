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
import com.example.foodapplication.utils.Constants;

import java.util.List;
import java.util.Locale;

public class DeliveryOrderAdapter extends RecyclerView.Adapter<DeliveryOrderAdapter.DeliveryViewHolder> {

    private final Context context;
    private final List<Order> orderList;
    private final OnDeliveryActionListener listener;

    public interface OnDeliveryActionListener {
        void onDeliveryAction(Order order, String nextStatus);
    }

    public DeliveryOrderAdapter(Context context, List<Order> orderList, OnDeliveryActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivery_order, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.getId());
        holder.tvStatus.setText(order.getOrderStatus());
        holder.tvPickupAddress.setText("Pickup: " + (order.getRestaurantName() != null ? order.getRestaurantName() : "Campus Food Court"));
        holder.tvDeliveryAddress.setText("Deliver to: " + order.getDeliveryAddress());
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotalAmount()));

        String currentStatus = order.getOrderStatus();
        String nextStatus;

        if (Constants.STATUS_READY.equals(currentStatus) || Constants.STATUS_PREPARING.equals(currentStatus)) {
            nextStatus = Constants.STATUS_PICKED_UP;
            holder.btnDeliveryAction.setText("Pick Up Order");
            holder.btnDeliveryAction.setVisibility(View.VISIBLE);
        } else if (Constants.STATUS_PICKED_UP.equals(currentStatus)) {
            nextStatus = Constants.STATUS_DELIVERED;
            holder.btnDeliveryAction.setText("Mark Delivered");
            holder.btnDeliveryAction.setVisibility(View.VISIBLE);
        } else {
            nextStatus = null;
            holder.btnDeliveryAction.setVisibility(View.GONE);
        }

        holder.btnDeliveryAction.setOnClickListener(v -> {
            if (listener != null && nextStatus != null) {
                listener.onDeliveryAction(order, nextStatus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class DeliveryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvPickupAddress, tvDeliveryAddress, tvTotal;
        Button btnDeliveryAction;

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPickupAddress = itemView.findViewById(R.id.tvPickupAddress);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnDeliveryAction = itemView.findViewById(R.id.btnDeliveryAction);
        }
    }
}
