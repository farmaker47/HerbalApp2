package com.example.herbalapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import static com.example.herbalapp.ModelConfig.CLASSIFICATION_THRESHOLD;
import static com.example.herbalapp.ModelConfig.CLASSIFICATION_RESULTS;

public class Classifier {

    private final Interpreter interpreter;

    public Classifier(Interpreter interpret) {
        this.interpreter = interpret;
    }

    public static Classifier classifier(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath);
        Interpreter interpreter = new Interpreter(byteBuffer);
        return new Classifier(interpreter);
    }

    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public List<Classification> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        float[][] result = new float[1][ModelConfig.Label.size()];
        interpreter.run(byteBuffer, result);
        return getSortedResult(result);
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
        int[] pixels = new int[ModelConfig.INPUT_WIDTH * ModelConfig.INPUT_HEIGHT];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixel : pixels) {
            float rChannel = (pixel >> 16) & 0xFF;
            float gChannel = (pixel >> 8) & 0xFF;
            float bChannel = (pixel) & 0xFF;
            float pixelValue = (rChannel + gChannel + bChannel) / 255.f;
            byteBuffer.putFloat(pixelValue);
        }
        return byteBuffer;
    }
}
