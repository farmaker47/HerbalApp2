package com.example.herbalapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import static com.example.herbalapp.ModelConfig.CLASSIFICATION_THRESHOLD;
import static com.example.herbalapp.ModelConfig.CLASSIFICATION_RESULTS;

public class Classifier {

    private final Interpreter interpreter;
    private Context mContext;

    public Classifier(Interpreter interpret, Context context) {
        this.interpreter = interpret;
        mContext = context;
    }

    public static Classifier classifier(AssetManager assetManager, String modelPath, Context context) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath, context);
        Interpreter interpreter = new Interpreter(byteBuffer);
        return new Classifier(interpreter, context);
    }

    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath, Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

    }

    public List<Classification> recognizeImage() {

        // soloupis
        // Fetches image from asset folder to view result from interpreter inference
        Bitmap assetsBitmap = getBitmapFromAsset(mContext, "7.jpg");
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(assetsBitmap, 32, 32, true);

        Bitmap prep = ImageUtils.preprocess(assetsBitmap);

        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
        Log.e("BYTE", Arrays.toString(new int[]{byteBuffer.array().length}));
        float[][] result = new float[1][ModelConfig.Label.size()];
        interpreter.run(byteBuffer, result);

        Log.e("RESULT", Arrays.toString(result[0]));

        return getSortedResult(result);
    }

    private Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

    private List<Classification> getSortedResult(float[][] result) {
        PriorityQueue<Classification> sortedResults = new PriorityQueue<>(CLASSIFICATION_RESULTS,
                (kiri, kanan) -> Float.compare(kanan.confidence, kiri.confidence)
        );

        for (int i = 0; i < ModelConfig.Label.size(); ++i) {
            float confidence = result[0][i];
            if (confidence > CLASSIFICATION_THRESHOLD) {
                ModelConfig.Label.size();
                sortedResults.add(new Classification(ModelConfig.Label.get(i), confidence));
            } else {
                //sortedResults.add(new Classification("Unknown", 0));
            }
        }
        return new ArrayList<>(sortedResults);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(ModelConfig.MODEL_INPUT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.rewind();

        int[] pixels = new int[ModelConfig.INPUT_WIDTH * ModelConfig.INPUT_HEIGHT];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Log.e("PIXELS", Arrays.toString(pixels));
        Log.e("PIXELS_SIZE", String.valueOf(pixels.length));

        /*for (int pixel : pixels) {
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            float pixelValue = (rChannel + gChannel + bChannel);
            byteBuffer.putFloat(pixelValue);
        }*/

        for (int k = 0; k < 10; k++) {
            int pixelValue = pixels[k];

            Log.e("PIXEL_NUMBER", String.valueOf(k));
            int R = Color.red(pixelValue);
            Log.e("PIXEL_VALUE_R", String.valueOf(R));
            int G = Color.green(pixelValue);
            Log.e("PIXEL_VALUE_G", String.valueOf(G));
            int B = Color.blue(pixelValue);
            Log.e("PIXEL_VALUE_B", String.valueOf(B));


        }

        float mean = 128.0f;
        float std = 128.0f;
        for (int i = 0; i < ModelConfig.INPUT_WIDTH; ++i) {
            for (int j = 0; j < ModelConfig.INPUT_WIDTH; ++j) {
                int pixelValue = pixels[i * ModelConfig.INPUT_WIDTH + j];

                /*Log.e("PIXEL_VALUE", String.valueOf(pixelValue));
                int A = Color.alpha(pixelValue);
                //Log.e("PIXEL_VALUE_A", String.valueOf(A));
                int R = Color.red(pixelValue);
                Log.e("PIXEL_VALUE_R", String.valueOf(R));
                int G = Color.green(pixelValue);
                Log.e("PIXEL_VALUE_G", String.valueOf(G));
                int B = Color.blue(pixelValue);
                Log.e("PIXEL_VALUE_B", String.valueOf(B));*/

                byteBuffer.putFloat((((pixelValue >> 16) & 0xFF)));
                byteBuffer.putFloat((((pixelValue >> 8) & 0xFF)));
                byteBuffer.putFloat(((pixelValue & 0xFF)));

            }
        }

        return byteBuffer;
    }
}
