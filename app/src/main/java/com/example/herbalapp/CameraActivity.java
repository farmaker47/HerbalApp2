package com.example.herbalapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKitView;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraActivity extends AppCompatActivity {

    @BindView(R.id.vCamera)
    CameraKitView vCamera;
    @BindView(R.id.ivPreview)
    ImageView ivPreview;
    @BindView(R.id.ivFinalPreview)
    ImageView ivFinalPreview;
    @BindView(R.id.tvClassification)
    TextView tvClassification;

    private Classifier Classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        loadClassifier();
    }

    private void loadClassifier() {
        try {
            Classifier = Classifier.classifier(getAssets(), ModelConfig.MODEL);
        } catch (IOException e) {
            Toast.makeText(this, "Model gagal dibaca.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        vCamera.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vCamera.onResume();
    }

    @Override
    protected void onPause() {
        vCamera.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        vCamera.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        vCamera.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick(R.id.btnTakePhoto)
    public void onTakePhoto() {
        vCamera.captureImage((cameraKitView, picture) -> {
            onImageCaptured(picture);
        });
    }

    private void onImageCaptured(byte[] picture) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        Bitmap original = ThumbnailUtils.extractThumbnail(bitmap, getScreenWidth(), getScreenWidth());
        //ivPreview.setImageBitmap(original);

        Bitmap prep = ImageUtils.preprocess(original);
        //ivFinalPreview.setImageBitmap(prep);

        List<Classification> hasil = Classifier.recognizeImage(prep);
        //tvClassification.setText(hasil.toString());

        //Bitmap bmp = prep;
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //bmp.compress(Bitmap.CompressFormat.PNG, 50, baos);
        //byte[] byteArray = baos.toByteArray();

        String output = hasil.toString();
        Toast.makeText(this, output, Toast.LENGTH_LONG).show();
        String[] split= output.split(",");
        //Toast.makeText(this, split[0], Toast.LENGTH_LONG).show();
        Intent intent =  new Intent(this, MainActivity.class);

        //Bundle extras = new Bundle();
        //extras.putString("Output",split[0]);
        //extras.putByteArray("Gambar", byteArray);
        //intent.putExtras(extras);

        String result = split[0];
        result = result.replace("[","");
        result = result.replace("]","");
        intent.putExtra("Output",result);
        startActivity(intent);
    }

    private int getScreenWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}