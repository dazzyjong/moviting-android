package com.moviting.android.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moviting.android.R;
import com.moviting.android.model.User;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FirstSettingActivity extends BaseActivity {

    private ImageView profileImage;
    private EditText nameText;
    private Spinner genderSpinner;
    private DatePicker birthdayPicker;
    private EditText favoriteMovieText;
    private EditText schoolText;
    private EditText workText;
    private EditText heightText;
    private EditText introduceText;

    private AlertDialog alertDialog;

    private String formattedBirthday;
    private String photoUrl;

    private static final String TAG = "FirstSettingActivity";
    public static int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_setting);

        profileImage = (ImageView) findViewById(R.id.profile_image);

        if (User.getUserInstance().photoUrl != null && !User.getUserInstance().photoUrl.equals("")) {
            Glide.with(this).load(User.getUserInstance().photoUrl).into(profileImage);
            photoUrl = User.getUserInstance().photoUrl;
        }

        nameText = (EditText) findViewById(R.id.account_profile_name);
        if (User.getUserInstance().name != null && !User.getUserInstance().name.equals("")) {
            nameText.setText(User.getUserInstance().name);
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

        if (User.getUserInstance().gender != null && !User.getUserInstance().gender.equals("")) {
            if (User.getUserInstance().gender.equals("male")) {
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
                User.getUserInstance().setMyAge(year);
                Log.d(TAG, "mylog" + formattedBirthday);
            }
        });

        if (User.getUserInstance().birthday != null && !User.getUserInstance().birthday.equals("")) {
            Calendar c = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy"); // here set the pattern as you date in string was containing like date/month/year
                c.setTime(sdf.parse(User.getUserInstance().birthday));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

            birthdayPicker.updateDate(year, month, dayOfMonth);
            User.getUserInstance().setMyAge(year);
        }

        favoriteMovieText = (EditText) findViewById(R.id.favorite_movie);
        schoolText = (EditText) findViewById(R.id.school);
        if (User.getUserInstance().school != null && !User.getUserInstance().school.equals("")) {
            schoolText.setText(User.getUserInstance().school);
        }
        workText = (EditText) findViewById(R.id.work);
        if (User.getUserInstance().work != null && !User.getUserInstance().name.equals("")) {
            workText.setText(User.getUserInstance().work);
        }
        heightText = (EditText) findViewById(R.id.height);
        introduceText = (EditText) findViewById(R.id.introduce);

        ImageButton photoButton = (ImageButton) findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog = createInflaterDialog();
                alertDialog.show();
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
        User user = User.getUserInstance();

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
        user.setIntroduce(introduceText.getText().toString());

        updateUserDataBase();
    }

    public void updateUserDataBase() {
        User user = User.getUserInstance();
        Map<String, Object> userValue = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + getUid(), userValue);
        getFirebaseDatabase().getReference().updateChildren(childUpdates).addOnCompleteListener(this, new OnCompleteListener<Void>() {
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
                        Intent libraryIntent = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        libraryIntent.setType("image/*");
                        startActivityForResult(
                                Intent.createChooser(libraryIntent, getString(R.string.select_file)),
                                SELECT_FILE);
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
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap src = BitmapFactory.decodeFile(selectedImagePath, options);
        profileImage.setImageBitmap(src);

        StorageReference profileImageReference = getFirebaseStorage().getReferenceFromUrl("gs://moviting.appspot.com/profile_image/");
        uploadImageToStorage(src, profileImageReference.child(getUid() + ".jpg"));
    }


    private void uploadImageToStorage(Bitmap bitmap, StorageReference reference) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getBaseContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
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

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, FirstSettingActivity.class);
        return in;
    }
}

