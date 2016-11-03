package com.moviting.android.ui.activity;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moviting.android.R;
import com.moviting.android.model.User;
import com.moviting.android.util.MyHashMap;
import com.moviting.android.util.PhotoFileUtility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_EDIT = 2;
    public static int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private static final String TAG = "ProfileActivity";
    private ListView profileList;
    private MyHashMap<String, Object> userProfile;
    private ImageView imageView;
    private User user;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFirebaseDatabaseReference().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Map map = user.toMap();
                userProfile = new MyHashMap<>(map);

                profileList = (ListView) findViewById(R.id.profile_property_list);
                profileList.setAdapter(new ProfileAdapter());

                profileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String selected = getResources().getStringArray(R.array.my_profile_list)[i];
                        startActivityForResult(
                                ProfilePropEditActivity.createIntent(getBaseContext(), selected, getValue(selected)),
                                REQUEST_EDIT);
                    }
                });
                imageView = (ImageView) findViewById(R.id.imageView);
                Glide.with(getBaseContext()).load(userProfile.get("photoUrl")).into(imageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });

        ImageButton imageButton = (ImageButton)findViewById(R.id.photo_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ProfileActivity.this,
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
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                ProfileActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, ProfileActivity.class);
        return in;
    }

    public class ProfileAdapter extends BaseAdapter {
        String[] profileList;

        ProfileAdapter() {
            profileList = getResources().getStringArray(R.array.my_profile_list);
        }
        @Override
        public int getCount() {
            return profileList.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ProfileAdapter.ProfileViewHolder profileViewHolder;
            if(view == null) {
                profileViewHolder = new ProfileAdapter.ProfileViewHolder();

                LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.profile_item, null);

                profileViewHolder.key = (TextView) view.findViewById(R.id.tag);
                profileViewHolder.value = (TextView) view.findViewById(R.id.value);
                profileViewHolder.arrow = (ImageView) view.findViewById(R.id.arrow);

                view.setTag(profileViewHolder);
            } else {
                profileViewHolder = (ProfileAdapter.ProfileViewHolder) view.getTag();
            }

            profileViewHolder.key.setText(profileList[i]);
            profileViewHolder.value.setText(getValue(profileList[i]));

            return view;

        }

        class ProfileViewHolder {
            TextView key;
            TextView value;
            ImageView arrow;
        }
    }

    private String getValue(String key) {
        if(key.equals("이름")) {
            return userProfile.get("name").toString();
        }
        if(key.equals("나이")) {
            return userProfile.get("myAge").toString();
        }
        if(key.equals("키")) {
            return userProfile.get("height").toString();
        }
        if(key.equals("학교")) {
            return userProfile.get("school").toString();
        }
        if(key.equals("직업")) {
            return userProfile.get("work").toString();
        }
        if(key.equals("인생 영화")) {
            return userProfile.get("favoriteMovie").toString();
        }
        return "";
    }

    private String getPropertyName(String key) {
        if(key.equals("이름")) {
            return "name";
        }
        if(key.equals("나이")) {
            return "myAge";
        }
        if(key.equals("키")) {
            return "height";
        }
        if(key.equals("학교")) {
            return "school";
        }
        if(key.equals("직업")) {
            return "work";
        }
        if(key.equals("인생 영화")) {
            return "favoriteMovie";
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_EDIT) {

                String key = data.getStringExtra("key");
                String value = data.getStringExtra("value");
                userProfile.put(getPropertyName(key), (Object) value);
                ((ProfileAdapter) profileList.getAdapter()).notifyDataSetChanged();
            }

            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap takenPicture = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(takenPicture);

        StorageReference profileImageReference = getFirebaseStorage().getReferenceFromUrl("gs://moviting.appspot.com/profile_image/");
        uploadImageToStorage(takenPicture, profileImageReference.child(getUid() + ".jpg"));
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        try {
            Bitmap src = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

            src = getResizedBitmap(src, 780);

            if (ContextCompat.checkSelfPermission(ProfileActivity.this,
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
            imageView.setImageBitmap(src);
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
                getFirebaseDatabaseReference().child("users").child(getUid()).child("photoUrl").setValue(downloadUrl.toString());
            }
        });

    }
}
