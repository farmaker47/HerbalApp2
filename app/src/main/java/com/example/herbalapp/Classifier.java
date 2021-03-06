package com.example.herbalapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;

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

import kotlin.jvm.internal.Intrinsics;

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

    /**
     * Crop Bitmap to maintain aspect ratio of model input.
     */
    private final Bitmap cropBitmap(Bitmap bitmap) {
        float bitmapRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        float modelInputRatio = 1.0F;
        double maxDifference = 1.0E-5D;
        float cropHeight = modelInputRatio - bitmapRatio;
        boolean var8 = false;
        if ((double) Math.abs(cropHeight) < maxDifference) {
            return bitmap;
        } else {
            Bitmap var10000;
            Bitmap croppedBitmap;
            if (modelInputRatio < bitmapRatio) {
                cropHeight = (float) bitmap.getHeight() - (float) bitmap.getWidth() / modelInputRatio;
                var10000 = Bitmap.createBitmap(bitmap, 0, (int) (cropHeight / (float) 2), bitmap.getWidth(), (int) ((float) bitmap.getHeight() - cropHeight));
                Intrinsics.checkExpressionValueIsNotNull(var10000, "Bitmap.createBitmap(\n   …toInt()\n                )");
                croppedBitmap = var10000;
            } else {
                cropHeight = (float) bitmap.getWidth() - (float) bitmap.getHeight() * modelInputRatio;
                var10000 = Bitmap.createBitmap(bitmap, (int) (cropHeight / (float) 2), 0, (int) ((float) bitmap.getWidth() - cropHeight), bitmap.getHeight());
                Intrinsics.checkExpressionValueIsNotNull(var10000, "Bitmap.createBitmap(\n   ….height\n                )");
                croppedBitmap = var10000;
            }

            return croppedBitmap;
        }
    }

    public List<Classification> recognizeImage() {

        // soloupis
        // Fetches image from asset folder to view result from interpreter inference
        Bitmap assetsBitmap = getBitmapFromAsset(mContext, "dogs.jpg");

        Bitmap croppedBitmap = cropBitmap(assetsBitmap);
        // https://developer.android.com/reference/android/graphics/Bitmap#createScaledBitmap(android.graphics.Bitmap,%20int,%20int,%20boolean)
        // true for Bilinear
        // false for Nearest Neighbour
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 32, 32, true);

        //Bitmap prep = ImageUtils.preprocess(assetsBitmap);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);


        // Initialization code
        // Create an ImageProcessor with all ops required. For more ops, please
        // refer to the ImageProcessor Architecture section in this README.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(32, 32, ResizeOp.ResizeMethod.BILINEAR))
                        //.add(new NormalizeOp(127.5f, 127.5f))
                        .build();

        // Create a TensorImage object. This creates the tensor of the corresponding
        // tensor type (uint8 in this case) that the TensorFlow Lite interpreter needs.
        TensorImage tImage = new TensorImage(DataType.FLOAT32);

        // Analysis code for every frame
        // Preprocess the image
        tImage.load(assetsBitmap);
        tImage = imageProcessor.process(tImage);

        //Log.e("BYTE", Arrays.toString(new int[]{byteBuffer.array().length}));
        float[][] result = new float[1][ModelConfig.Label.size()];

        // Create a container for the result and specify that this is a quantized model.
        // Hence, the 'DataType' is defined as UINT8 (8-bit unsigned integer)
        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, 10}, DataType.FLOAT32);

        interpreter.run(tImage.getBuffer(), probabilityBuffer.getBuffer());

        Log.e("RESULT", Arrays.toString(probabilityBuffer.getFloatArray())/*Arrays.toString(result[0])*/);


        /// Task library
        // Initialization
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(3).build();
        ObjectDetector objectDetector = null;
        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(mContext, "object.tflite", options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Run inference
        List<Detection> results = null;
        if (objectDetector != null) {
            results = objectDetector.detect(tImage);
        }
        if (results != null) {
            Log.e("OBJECT_DETECTOR", results.toString());
        }
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

        float mean = 0.0f;
        float std = 1.0f;
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

                byteBuffer.putFloat((((pixelValue >> 16) & 0xFF) - mean) / std);
                byteBuffer.putFloat((((pixelValue >> 8) & 0xFF) - mean) / std);
                byteBuffer.putFloat(((pixelValue & 0xFF) - mean) / std);

            }
        }

        return byteBuffer;
    }
}
