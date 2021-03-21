package com.example.project;

import android.graphics.Bitmap;

public class PixelBuffer {
    int[] buffer;
    int[] shape;
    public PixelBuffer(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        buffer = new int[width * height];
        shape = new int[]{height, width, 1};
        bitmap.getPixels(buffer, 0, width, 0, 0, width, height);
    }
}
