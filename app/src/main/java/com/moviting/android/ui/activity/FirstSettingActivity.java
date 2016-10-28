package com.moviting.android.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moviting.android.R;
import com.moviting.android.model.User;
import com.moviting.android.util.PhotoFileUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FirstSettingActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private ImageView profileImage;
    private EditText nameText;
    private Spinner genderSpinner;
    private DatePicker birthdayPicker;
    private EditText favoriteMovieText;
    private EditText schoolText;
    private EditText workText;
    private EditText heightText;

    private AlertDialog alertDialog;

    private String formattedBirthday;
    private String photoUrl;

    private User user;

    private static final String TAG = "FirstSettingActivity";
    public static int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user = getIntent().getExtras().getParcelable("user");
        profileImage = (ImageView) findViewById(R.id.profile_image);

        if (user.photoUrl != null && !user.photoUrl.equals("")) {
            Glide.with(this).load(user.photoUrl).into(profileImage);
            photoUrl = user.photoUrl;
        }

        nameText = (EditText) findViewById(R.id.account_profile_name);
        if (user.name != null && !user.name.equals("")) {
            nameText.setText(user.name);
        }

        genderSpinner = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "mylog onItemSelected");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "mylog onNothingSelected");
            }
        });

        if (user.gender != null && !user.gender.equals("")) {
            if (user.gender.equals("male")) {
                genderSpinner.setSelection(1);
            } else {
                genderSpinner.setSelection(2);
            }
        }

        birthdayPicker = (DatePicker) findViewById(R.id.birthday);
        birthdayPicker.init(1970, 0, 1, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                int month = monthOfYear + 1;
                String formattedMonth = "" + month;
                String formattedDayOfMonth = "" + dayOfMonth;

                if (month < 10) {

                    formattedMonth = "0" + month;
                }
                if (dayOfMonth < 10) {

                    formattedDayOfMonth = "0" + dayOfMonth;
                }

                formattedBirthday =   formattedMonth + "/" + formattedDayOfMonth + "/" + year;
                user.transferBirthYearToMyAge(year);
                Log.d(TAG, "mylog" + formattedBirthday);
            }
        });

        if (user.birthday != null && !user.birthday.equals("")) {
            Calendar c = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // here set the pattern as you date in string was containing like date/month/year
                c.setTime(sdf.parse(user.birthday));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

            birthdayPicker.updateDate(year, month, dayOfMonth);
            user.transferBirthYearToMyAge(year);
        }

        favoriteMovieText = (EditText) findViewById(R.id.favorite_movie);
        schoolText = (EditText) findViewById(R.id.school);
        if (user.school != null && !user.school.equals("")) {
            schoolText.setText(user.school);
        }
        workText = (EditText) findViewById(R.id.work);
        if (user.work != null && !user.name.equals("")) {
            workText.setText(user.work);
        }
        heightText = (EditText) findViewById(R.id.height);

        ImageButton photoButton = (ImageButton) findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(FirstSettingActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(FirstSettingActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                    } else{
                        alertDialog = createInflaterDialog();
                        alertDialog.show();
                    }
                } else {
                    alertDialog = createInflaterDialog();
                    alertDialog.show();
                }
            }
        });

        Button submitButton = (Button) findViewById(R.id.account_setting_submit);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog();
                if (validateForm()) {
                    updateUser();
                } else {
                    hideProgressDialog();
                }
            }
        });
    }

    public boolean validateForm() {
        boolean valid = true;

        if (photoUrl != null && photoUrl.equals("")) {
            Toast.makeText(this, R.string.photo_required, Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (nameText.getText().toString().trim().equals("")) {
            nameText.setError(getString(R.string.name_required));
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (genderSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.gender_required, Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (birthdayPicker.getDayOfMonth() == 1 && birthdayPicker.getMonth() == 1 && birthdayPicker.getYear() == 1970) {
            Toast.makeText(this, R.string.gender_required, Toast.LENGTH_LONG).show();
            valid = false;
        }

        if (favoriteMovieText.getText().toString().trim().equals("")) {
            favoriteMovieText.setError(getString(R.string.movie_required));
            valid = false;
        } else {
            favoriteMovieText.setError(null);
        }

        if (schoolText.getText().toString().trim().equals("")) {
            schoolText.setError(getString(R.string.school_required));
            valid = false;
        } else {
            schoolText.setError(null);
        }

        if (workText.getText().toString().trim().equals("")) {
            workText.setError(getString(R.string.work_required));
            valid = false;
        } else {
            workText.setError(null);
        }

        if (heightText.getText().toString().trim().equals("")) {
            heightText.setError(getString(R.string.height_required));
            valid = false;
        } else {
            heightText.setError(null);
        }

        return valid;
    }

    public void updateUser() {
        user.setName(nameText.getText().toString());
        user.setPhotoUrl(photoUrl);
        if( genderSpinner.getSelectedItem().equals(getResources().getStringArray(R.array.gender)[1]) ) {
            user.setGender("male");
        } else {
            user.setGender("female");
        }
        user.setBirthday(formattedBirthday);
        user.setFavoriteMovie(favoriteMovieText.getText().toString());
        user.setSchool(schoolText.getText().toString());
        user.setWork(workText.getText().toString());
        user.setHeight(heightText.getText().toString());
        user.setToken(getToken());

        updateUserDataBase();
    }

    public void updateUserDataBase() {

        Map<String, Object> userValue = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + getUid(), userValue);
        getFirebaseDatabaseReference().updateChildren(childUpdates).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(FirstSettingActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    startActivity(MainActivity.createIntent(FirstSettingActivity.this));
                    finish();
                }
                hideProgressDialog();
            }
        });
    }


    private AlertDialog createInflaterDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(R.string.profile_photo);
        ab.setItems(R.array.choose_photo_resource, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
                        setDismiss(alertDialog);
                        break;
                    case 1:
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), SELECT_FILE);
                        setDismiss(alertDialog);
                        break;
                    default:
                        break;

                }
            }
        });

        ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                setDismiss(alertDialog);
            }
        });

        return ab.create();
    }

    private void setDismiss(Dialog dialog) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap takenPicture = (Bitmap) data.getExtras().get("data");
        profileImage.setImageBitmap(takenPicture);

        StorageReference profileImageReference = getFirebaseStorage().getReferenceFromUrl("gs://moviting.appspot.com/profile_image/");
        uploadImageToStorage(takenPicture, profileImageReference.child(getUid() + ".jpg"));
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        try {
            Bitmap src = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

            src = getResizedBitmap(src, 780);

            if (ContextCompat.checkSelfPermission(FirstSettingActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                int orientation = getRotation(selectedImageUri);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        src = rotateBitmap(src, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        src = rotateBitmap(src, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        src = rotateBitmap(src, 270);
                        break;
                }
            }
            profileImage.setImageBitmap(src);
            StorageReference profileImageReference = getFirebaseStorage().getReferenceFromUrl("gs://moviting.appspot.com/profile_image/");
            uploadImageToStorage(src, profileImageReference.child(getUid() + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {

                alertDialog = createInflaterDialog();
                alertDialog.show();
                return;
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private int getRotation(Uri bitmapUri) {
        int result = 0;

        String path = PhotoFileUtility.getRealPathFromURI(this, bitmapUri);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(exif != null) {
            result = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }

        return result;
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

    private void uploadImageToStorage(Bitmap bitmap, StorageReference reference) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                photoUrl = downloadUrl.toString();
            }
        });

    }

    private void signOut() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();
                if (providerId.equals("facebook.com")) {
                    LoginManager.getInstance().logOut();
                }
            }

            mAuth.signOut();
        }

        startActivity(LoginActivity.createIntent(this));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                signOut();
                break;
            case R.id.action_complete:

                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_pref, menu);
        return true;
    }

    public static Intent createIntent(Context context, User user) {
        Intent in = new Intent();
        in.putExtra("user", user);
        in.setClass(context, FirstSettingActivity.class);
        return in;
    }
}

