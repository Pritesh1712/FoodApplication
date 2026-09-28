package com.example.foodapplication.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.foodapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Converts a local Uri from Android Gallery into a compressed Base64 string
     */
    public static String uriToBase64(Context context, Uri uri) {
        if (context == null || uri == null) return "";
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) return uri.toString();

            // Resize bitmap to max 600px width/height to keep payload small for DB
            int maxDim = 600;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = Math.min((float) maxDim / width, (float) maxDim / height);

            if (ratio < 1.0f) {
                int newWidth = Math.round(width * ratio);
                int newHeight = Math.round(height * ratio);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return uri.toString();
        }
    }

    /**
     * Safely loads an image string (Base64, HTTP URL, file, content Uri) into an ImageView using Glide
     */
    public static void loadImage(Context context, String imageSource, ImageView imageView) {
        if (context == null || imageView == null) return;

        if (imageSource == null || imageSource.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        try {
            if (imageSource.startsWith("data:image/") && imageSource.contains("base64,")) {
                String pureBase64 = imageSource.substring(imageSource.indexOf("base64,") + 7);
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                Glide.with(context)
                        .load(decodedBytes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageView);
            } else if (imageSource.startsWith("content://") || imageSource.startsWith("file://")) {
                Glide.with(context)
                        .load(Uri.parse(imageSource))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageView);
            } else {
                Glide.with(context)
                        .load(imageSource)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
    }
}
