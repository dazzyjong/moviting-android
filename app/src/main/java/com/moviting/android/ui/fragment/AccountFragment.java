package com.moviting.android.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.moviting.android.R;
import com.moviting.android.ui.activity.CouponActivity;
import com.moviting.android.ui.activity.LoginActivity;
import com.moviting.android.ui.activity.ProfileActivity;
import com.moviting.android.ui.activity.WebViewActivity;

public class AccountFragment extends BaseFragment {

    private static final String TAG = "AccountFragment";
    private static final int ACCOUNT_MODE = 2;

    private ListView accountTabList;
    private FirebaseAuth mAuth;
    private AlertDialog alertDialog;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        accountTabList = (ListView) view.findViewById(R.id.account_tab_list);
        String[] values = new String[] {
                getResources().getString(R.string.my_profile),
                getResources().getString(R.string.coupon_box),
                getResources().getString(R.string.FAQ),
                getResources().getString(R.string.Ask),
                getResources().getString(R.string.logout)
        };

        createAccountTabList(accountTabList, values);

        accountTabList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        startActivity(ProfileActivity.createIntent(getActivity()));
                        break;
                    case 1:
                        startActivity(CouponActivity.createIntent(getActivity(), ACCOUNT_MODE));
                        break;
                    case 2:
                        startActivity(WebViewActivity.createIntent(getActivity(), "http://theysy.com/qna.html"));
                        break;
                    case 3:
                        startActivity(WebViewActivity.createIntent(getActivity(), "http://plus.kakao.com/home/@%EC%97%B0%EC%8B%9C%EC%98%81"));
                        break;
                    case 4:
                        createWarningDialog();
                        break;
                    default:
                        break;
                }
            }
        });

        return view;
    }

    private void createWarningDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        // AlertDialog 셋팅
        alertDialogBuilder
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        signOut();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });

        // 다이얼로그 생성
        alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();
    }

    public void signOut() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();
                if (providerId.equals("facebook.com")) {
                    LoginManager.getInstance().logOut();
                }
            }
            getFirebaseDatabaseReference().child("users").child(getUid()).child("token").removeValue();
            mAuth.signOut();
        }

        startActivity(LoginActivity.createIntent(getActivity()));
        getActivity().finish();
    }

    public void createAccountTabList(ListView view, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_list_item, R.id.text1, values);
        view.setAdapter(adapter);
    }
}
