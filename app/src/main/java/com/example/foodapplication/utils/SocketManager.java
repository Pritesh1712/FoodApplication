package com.example.foodapplication.utils;

import android.util.Log;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {

    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket socket;

    public interface OnStatusChangeListener {
        void onStatusChanged(String orderId, String newStatus);
    }

    private SocketManager() {
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            socket = IO.socket(Constants.SOCKET_URL, opts);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket URI Syntax Error: " + e.getMessage());
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            Log.d(TAG, "Socket connecting...");
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            Log.d(TAG, "Socket disconnected.");
        }
    }

    public void joinOrderRoom(String orderId) {
        if (socket != null) {
            socket.emit("join_order_room", orderId);
        }
    }

    public void listenForOrderStatus(OnStatusChangeListener listener) {
        if (socket != null) {
            socket.on("order_status_updated", args -> {
                if (args.length > 0 && listener != null) {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        String orderId = data.getString("orderId");
                        String status = data.getString("status");
                        listener.onStatusChanged(orderId, status);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing socket event: " + e.getMessage());
                    }
                }
            });
        }
    }
}
