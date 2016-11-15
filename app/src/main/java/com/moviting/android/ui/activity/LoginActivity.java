package com.moviting.android.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private FirebaseAuth.AuthStateListener mAuthListener;
    // Due to Firebase onAuthStateChanged bug which invoked multiple time,
    // need to set flag when user sign in.
    private boolean mIsAuthed;

    private CallbackManager mCallbackManager;

    private EditText mEmail;
    private EditText mPassword;
    private Button mSigninPassword;
    private Button mCreateAccount;
    private Button mSigninFaceBook;

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            //Do what you want
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                //This dialog will help the user update to the latest GooglePlayServices
                dialog.show();
            }
        }

        if (getFirebaseAuth().getCurrentUser() != null) {
            showProgressDialog();
        }

        if(!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        setContentView(R.layout.activity_login);

        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);
        mSigninPassword = (Button)findViewById(R.id.signin_password);
        mCreateAccount = (Button)findViewById(R.id.create_accout);
        mSigninFaceBook = (Button)findViewById(R.id.signin_facebook);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email",
                "public_profile",
                "user_work_history",
                "user_education_history",
                "user_birthday");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser onAuthedUser = firebaseAuth.getCurrentUser();
                if (onAuthedUser != null && onAuthedUser == getFirebaseAuth().getCurrentUser() && !mIsAuthed) {
                    // Myself
                    Log.d(TAG, "onAuthStateChanged:signed_in:myself" + onAuthedUser.getUid());
                    mIsAuthed = true;

                    createOrUpdateUserAndDatabase();

                } else if(onAuthedUser != null) {
                    // A user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + onAuthedUser.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        mSigninPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                signIn(email, password);
            }
        });

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                createAccount(email, password);
            }
        });

        mSigninFaceBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == getResources().getInteger(R.integer.submit_action_id) || actionId == EditorInfo.IME_NULL) {
                    String email = mEmail.getText().toString();
                    String password = mPassword.getText().toString();
                    signIn(email, password);
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getFirebaseAuth().addAuthStateListener(mAuthListener);
        mIsAuthed = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            getFirebaseAuth().removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mEmail.setError(e.getMessage());
                    }
        });
    }

    public void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        getFirebaseAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            hideProgressDialog();
                        }

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e.getMessage().contains("password")){
                            mPassword.setError(e.getMessage());
                        } else {
                            mEmail.setError(e.getMessage());
                        }
                    }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Required.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Required.");
            valid = false;
        } else if(password.length() < 4 ) {
            mPassword.setError("Too Short.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 64206){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            LoginManager.getInstance().logOut();
                            hideProgressDialog();
                        }
                    }
                });
    }

    public void createOrUpdateUserAndDatabase() {
        // Check Database has user account
        getFirebaseDatabaseReference().child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(user == null) {
                    Log.d(TAG, "onDataChange user null");
                    // No user, this is first login
                    // 1. Create User model
                    user = createUserFromFirebaseUser(getFirebaseAuth().getCurrentUser());

                    // 2. check if it is facebook account
                    if(isFaceBookAccount()) {
                        try {
                            setUserPropertyFromFaceBook(user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 3. update user info to data base
                        updateUserDataBase(user);
                        startActivityUnderCondition(true, user);
                    }
                } else if(!user.isUserFormFilled()) {
                    Log.d(TAG, "onDataChange filled_account_info false");
                    startActivityUnderCondition(true, user);
                } else {
                    // There is user, this is revisit
                    // 1. read user info from database
                    Log.d(TAG, "onDataChange success");
                    if (user.token == null || !user.token.equals(getToken())) {
                        getFirebaseDatabaseReference().child("users").child(getUid()).child("token").setValue(getToken());
                    }
                    startActivityUnderCondition(false, user);
                }

                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    public boolean isFaceBookAccount() {
        FirebaseUser user = getFirebaseAuth().getCurrentUser();
        boolean result = false;
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();

                switch (providerId) {
                    case "facebook.com":
                        result = true;
                        break;

                    default:
                        break;

                }
            }
        }
        return result;
    }

    public void updateUserDataBase(User user) {
        Map<String, Object> userValue = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + getUid(), userValue);
        getFirebaseDatabaseReference().updateChildren(childUpdates);
    }

    public void setUserPropertyFromFaceBook(final User user) throws JSONException{

        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                                                            new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject obj, GraphResponse response) {
                Log.d(TAG, "facebook user: " + obj + " / " + response.getError());


                try {
                    if(obj.has("birthday")) {
                        user.setBirthday(obj.getString("birthday"));
                    }
                    if(obj.has("gender")) {
                        user.setGender(obj.getString("gender"));
                    }
                    if(obj.has("education")) {
                        for(int i = 0; i < obj.getJSONArray("education").length(); i++){
                            if(obj.getJSONArray("education").getJSONObject(i).getString("type")
                                    .equals("College")) {
                                user.setSchool(obj.getJSONArray("education").getJSONObject(i)
                                        .getJSONObject("school").getString("name"));
                            }
                        }
                    }
                    if(obj.has("work")) {
                        for(int i = 0; i < obj.getJSONArray("work").length(); i++) {
                            if(!obj.getJSONArray("work").getJSONObject(i).has("end_date")) {
                                user.setWork(obj.getJSONArray("work").getJSONObject(i)
                                        .getJSONObject("employer").getString("name"));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                updateUserDataBase(user);
                startActivityUnderCondition(true, user);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "birthday,gender,education,work");
        request.setParameters(parameters);

        request.executeAsync();
    }

    public User createUserFromFirebaseUser(FirebaseUser fbUser) {
        String name = "";
        String email = "";

        if(fbUser.getDisplayName() != null) {
            name = fbUser.getDisplayName();
        }
        if(fbUser.getEmail() != null) {
            email = fbUser.getEmail();
        }

        return new User(name, email);
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, LoginActivity.class);
        return in;
    }

    public void startActivityUnderCondition(boolean isFirst, User user) {
        if(isFirst) {
            startActivity(FirstSettingActivity.createIntent(LoginActivity.this, user));
            finish();
        } else {
            startActivity(MainActivity.createIntent(LoginActivity.this));
            finish();
        }
    }
}
