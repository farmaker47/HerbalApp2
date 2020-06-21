package com.example.herbalapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageUtils {

    private static final ColorMatrix BINARY = new ColorMatrix(
            new float[]{
                    255, 0, 0, 0, -128*255,
                    0, 255, 0, 0, -128*255,
                    0, 0, 255, 0, -128*255,
                    0, 0, 0, 1, 0
            });

    private static final ColorMatrix ORIGINAL = new ColorMatrix(
            new float[]{
                    1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0,
                    0, 0, 1, 0, 0,
                    0, 0, 0, 1, 0
            });

    private static final ColorMatrix INVERT = new ColorMatrix(
            new float[]{
                    -1, 0, 0, 0, 255,
                    0, -1, 0, 0, 255,
                    0, 0, -1, 0, 255,
                    0.5f, 0.5f, 0.5f, 0, 0,
                    0, 0, 0, 1, 0
            });

    private static final ColorMatrix BLACKWHITE = new ColorMatrix(
            new float[]{
                    0.5f, 0.5f, 0.5f, 0, 0,
                    0.5f, 0.5f, 0.5f, 0, 0,
                    0, 0, 0, 1, 0,
                    -1, -1, -1, 0, 1
            }
    );

    public static Bitmap preprocess(Bitmap bitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        //colorMatrix.setSaturation(0);
        //colorMatrix.postConcat(BINARY);
        //colorMatrix.postConcat(ORIGINAL);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(colorMatrix);

        Paint paint = new Paint();
        paint.setColorFilter(f);

        Bitmap bmpColor = Bitmap.createScaledBitmap(bitmap, ModelConfig.INPUT_WIDTH, ModelConfig.INPUT_HEIGHT, false);
        Canvas canvas = new Canvas(bmpColor);
        canvas.drawBitmap(bmpColor, 0, 0, paint);
        return bmpColor;
    }
}
