package com.example.herbalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    Button mCaptureBtn;
    ImageView mImageView;
    TextView judul1,judul2,judul3, nama, namaLatin, khasiat, cr, overview;

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overview=findViewById(R.id.overview);
        mImageView = findViewById(R.id.image_view);
        mCaptureBtn = findViewById(R.id.capture_image_btn);
        judul1 = findViewById(R.id.judul1);
        judul2 = findViewById(R.id.judul2);
        judul3 = findViewById(R.id.judul3);
        cr = findViewById(R.id.copyright);
        nama = findViewById(R.id.nama);
        namaLatin = findViewById(R.id.namaLatin);
        khasiat = findViewById(R.id.khasiat);

        judul1.setText(R.string.tutorial);
        judul2.setText(R.string.kosong);
        judul3.setText(R.string.kosong);
        nama.setText(R.string.kosong);
        overview.setText(R.string.kosong);
        namaLatin.setText(R.string.kosong);
        khasiat.setText(R.string.kosong);


        Intent intent = getIntent();
        if (intent.getStringExtra("Output") != null){
            //Bundle extras = intent.getExtras();
            //String hasil = extras.getString("Output");
            //Bitmap bmp = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("Gambar"),0,getIntent().getByteArrayExtra("Gambar").length);
            //mImageView.setImageBitmap(bmp);
            String hasil =  intent.getStringExtra("Output");
            prediksi(hasil);
            //Toast.makeText(this, hasil, Toast.LENGTH_SHORT).show();
        }

        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                        String[] permission = {Manifest.permission.CAMERA};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });
    }

    private void openCamera(){
        Intent intent = new Intent (this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void prediksi(String s) {
        judul1.setText(R.string.judul1);
        judul2.setText(R.string.judul2);
        judul3.setText(R.string.judul3);
        overview.setText(s);
        if (s.contains("Cocomint")){
            nama.setText(R.string.n0);
            namaLatin.setText(R.string.nl0);
            khasiat.setText(R.string.k0);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p0);
            mImageView.setImageDrawable(myDrawable);
        } else if (s.contains("Daunungu")){
            nama.setText(R.string.n1);
            namaLatin.setText(R.string.nl1);
            khasiat.setText(R.string.k1);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p1);
            mImageView.setImageDrawable(myDrawable);
        } else if (s.contains("Jerukpurut")){
            nama.setText(R.string.n2);
            namaLatin.setText(R.string.nl2);
            khasiat.setText(R.string.k2);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p2);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Kejibeling")){
            nama.setText(R.string.n3);
            namaLatin.setText(R.string.nl3);
            khasiat.setText(R.string.k3);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p3);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Kelor")){
            nama.setText(R.string.n4);
            namaLatin.setText(R.string.nl4);
            khasiat.setText(R.string.k4);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p4);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Kumiskucing")){
            nama.setText(R.string.n5);
            namaLatin.setText(R.string.nl5);
            khasiat.setText(R.string.k5);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p5);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Lemonbalm")){
            nama.setText(R.string.n6);
            namaLatin.setText(R.string.nl6);
            khasiat.setText(R.string.k6);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p6);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Orangemint")){
            nama.setText(R.string.n7);
            namaLatin.setText(R.string.nl7);
            khasiat.setText(R.string.k7);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p7);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Spearmint")){
            nama.setText(R.string.n8);
            namaLatin.setText(R.string.nl8);
            khasiat.setText(R.string.k8);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p8);
            mImageView.setImageDrawable(myDrawable);
        }else if (s.contains("Stevia")){
            nama.setText(R.string.n9);
            namaLatin.setText(R.string.nl9);
            khasiat.setText(R.string.k9);
            Drawable myDrawable = getResources().getDrawable(R.drawable.p9);
            mImageView.setImageDrawable(myDrawable);
        }else {
            nama.setText(R.string.no);
            namaLatin.setText(R.string.no);
            khasiat.setText(R.string.no);
        }
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
