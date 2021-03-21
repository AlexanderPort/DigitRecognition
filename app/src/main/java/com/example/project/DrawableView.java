package com.example.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.lifecycle.Lifecycle;

import org.tensorflow.lite.Interpreter;
import org.w3c.dom.Attr;

import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class DrawableView extends View {
    Paint paint;
    Canvas canvas;
    Bitmap bitmap;
    boolean touched;
    float radius = 30;
    int width, height;
    DisplayMetrics metrics;
    float[] touch1, touch2;
    float minX, minY, maxX, maxY;
    public void init() {
        metrics = Resources.getSystem().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        initBoundingBox();

    }
    public DrawableView(Context context) {
        super(context);
        init();
    }
    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public DrawableView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (touched) {
            touched = false;
            if (touch1 != null && touch2 != null) {
                paint.setColor(Color.BLACK);
                drawLine(this.canvas, touch1[0], touch1[1], touch2[0], touch2[1]);
                touch1 = touch2; touch2 = null;
            }
        }

        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            touched = true;
            if (touch1 == null) {
                touch1 = new float[]{event.getX(), event.getY()};
                updateBoundingBox(event);
            } else if (touch2 == null) {
                touch2 = new float[]{event.getX(), event.getY()};
                updateBoundingBox(event);
            }
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            touch1 = null; touch2 = null;
        }
        return true;
    }
    public void drawLine(Canvas canvas, float x1, float y1, float x2, float y2) {
        if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
            if (x2 < x1) {
                float x3 = x2; float y3 = y2;
                x2 = x1; y2 = y1; x1 = x3; y1 = y3;
            }
            float a = (y2 - y1) / (x2 - x1); float y = y1;
            for (float x = x1; x < x2; x++) {
                canvas.drawCircle(x, y, radius, paint);
                y += a;
            }
        } else {
            if (y2 < y1) {
                float x3 = x2; float y3 = y2;
                x2 = x1; y2 = y1; x1 = x3; y1 = y3;
            }
            float a = (x2 - x1) / (y2 - y1); float x = x1;
            for (float y = y1; y < y2; y++) {
                canvas.drawCircle(x, y, radius, paint);
                x += a;
            }
        }
    }
    public PixelBuffer getPixelBuffer(Bitmap bitmap) {
        return new PixelBuffer(preprocess(bitmap));
    }
    public Bitmap crop(Bitmap bitmap, Rect rect) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap cropped;
        if (0 < rect.left && rect.right < width &&
                0 < rect.top && rect.bottom < height) {
            cropped = Bitmap.createBitmap(bitmap, rect.left, rect.top,
                    rect.right - rect.left, rect.bottom - rect.top);
        } else {
            int left = 0, right = 0, top = 0, bottom = 0;
            if (0 > rect.left) left = Math.abs(rect.left);
            if (rect.right > width) right = rect.right - width;
            if (0 > rect.top) top = Math.abs(rect.top);
            if (rect.bottom > height) bottom = rect.bottom - height;

            Bitmap expanded = Bitmap.createBitmap(
                    width + left + right,
                    height + top + bottom,
                    Bitmap.Config.ARGB_8888);
            expanded.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(expanded);
            canvas.drawBitmap(bitmap, left, top, null);
            int w = rect.right - rect.left;
            int h = rect.bottom - rect.top;
            int x = Math.max(rect.left, 0);
            int y = Math.max(rect.top, 0);
            cropped = Bitmap.createBitmap(expanded, x, y, w, h);
            expanded.recycle();
        }
        return cropped;
    }
    public Bitmap preprocess(Bitmap bitmap) {
        if (maxX != Float.MIN_VALUE && maxY != Float.MIN_VALUE &&
                minX != Float.MAX_VALUE && minY != Float.MAX_VALUE) {
            int width = (int)(maxX - minX);
            int height = (int)(maxY - minY);
            int centerX = (int)(minX + width / 2);
            int centerY = (int)(minY + height / 2);
            int length = Math.max(width, height) + 100;
            Rect rect = new Rect(centerX - length / 2, centerY - length / 2,
                    centerX + length / 2, centerY + length / 2);
            return crop(bitmap, rect);
        }
        return null;
    }
    public void clear() {
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }
    void updateBoundingBox(MotionEvent event) {
        maxX = Math.max(event.getX(), maxX);
        maxY = Math.max(event.getY(), maxY);
        minX = Math.min(event.getX(), minX);
        minY = Math.min(event.getY(), minY);
    }
    void initBoundingBox() {
        maxX = Float.MIN_VALUE;
        maxY = Float.MIN_VALUE;
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
    }
}
