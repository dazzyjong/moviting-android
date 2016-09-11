package com.moviting.android.ui.fragment;

import android.support.v4.app.Fragment;

import com.moviting.android.ui.activity.BaseActivity;

public class BaseFragment extends Fragment {

    public BaseActivity getBaseActivity() {
        return (BaseActivity)super.getActivity();
    }

}
