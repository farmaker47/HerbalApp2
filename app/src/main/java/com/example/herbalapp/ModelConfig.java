package com.example.herbalapp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModelConfig {
    public static String MODEL = "converted_model2.tflite";

    public static final int INPUT_WIDTH = 32;
    public static final int INPUT_HEIGHT = 32;
    public static final int DIMENSION_SIZE = 4;
    public static final int CHANNEL_RGB = 3;
    public static final int MODEL_INPUT_SIZE = DIMENSION_SIZE * INPUT_WIDTH * INPUT_HEIGHT * CHANNEL_RGB;

    public static final List<String> Label = Collections.unmodifiableList(Arrays.asList("Cocomint", "Daunungu", "Jerukpurut", "Kejibeling", "Kelor", "Kumiskucing", "Lemonbalm", "Orangemint", "Spearmint", "Stevia"));

    public static final int CLASSIFICATION_RESULTS = 3;
    public static final float CLASSIFICATION_THRESHOLD = 0.1f;
}
