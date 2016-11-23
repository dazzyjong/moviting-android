package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
        setImageCropViewFromGalleryResult(getIntent());
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

    private void setImageCropViewFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        try {
            src = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            src = getResizedBitmap(src, 780);
            imageCropView.setImageBitmap(src);
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
