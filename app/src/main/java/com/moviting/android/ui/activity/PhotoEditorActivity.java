package com.moviting.android.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.moviting.android.R;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoEditorActivity extends BaseActivity {

    private static final String TAG = "PhotoEditorActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private ImageCropView imageCropView;
    private Button cropButton;
    private Button leftButton;
    private Button rightButton;
    private Button ratio34;
    private Button ratio43;
    private Bitmap src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageCropView = (ImageCropView) findViewById(R.id.imageCropView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PhotoEditorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PhotoEditorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                setImageCropViewFromGalleryResult(getIntent());
            }
        } else {
            setImageCropViewFromGalleryResult(getIntent());
        }

        imageCropView.setAspectRatio(4, 3);

        cropButton = (Button) findViewById(R.id.cropButton);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                if(!imageCropView.isChangingScale()) {
                    Bitmap b = imageCropView.getCroppedImage();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytes = stream.toByteArray();

                    String filename = "bitmap";
                    FileOutputStream outputStream;

                    try {
                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        outputStream.write(bytes);
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                intent.putExtra("bitmap", "bitmap");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        leftButton = (Button) findViewById(R.id.rotateLeft);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateBitmap(270);
            }
        });
        rightButton = (Button) findViewById(R.id.rotateRight);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateBitmap(90);
            }
        });

        ratio34 = (Button) findViewById(R.id.ratio34);
        ratio34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCropView.setAspectRatio(3, 4);
            }
        });

        ratio43 = (Button) findViewById(R.id.ratio43);
        ratio43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageCropView.setAspectRatio(4, 3);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                setImageCropViewFromGalleryResult(getIntent());
            }
        }
    }

    private void setImageCropViewFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        try {
            if (ContextCompat.checkSelfPermission(PhotoEditorActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                src = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                src = getResizedBitmap(src, 780);
                imageCropView.setImageBitmap(src);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rotateBitmap(int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        src = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        imageCropView.setImageBitmap(src);
    }

    public static Intent createIntent(Context context, Intent intent) {
        intent.setClass(context, PhotoEditorActivity.class);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                PhotoEditorActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
