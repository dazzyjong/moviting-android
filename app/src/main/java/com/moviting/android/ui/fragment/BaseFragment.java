package com.moviting.android.ui.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.moviting.android.R;
import com.moviting.android.ui.activity.BaseActivity;
import com.moviting.android.util.DatabaseHelper;

public class BaseFragment extends Fragment {
    public ProgressDialog mProgressDialog;
    public void showProgressDialog() {
        Activity activity = getActivity();
        if(activity != null) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(getString(R.string.loading));
                mProgressDialog.setIndeterminate(true);
            }
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
    public String getToken() {
        return FirebaseInstanceId.getInstance().getToken();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public DatabaseReference getFirebaseDatabaseReference() {
        return DatabaseHelper.getInstance().getReference();
    }
}
