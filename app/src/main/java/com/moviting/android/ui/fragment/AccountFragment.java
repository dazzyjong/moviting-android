package com.moviting.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.moviting.android.R;
import com.moviting.android.model.User;
import com.moviting.android.ui.activity.LoginActivity;
import com.moviting.android.ui.activity.ProfileActivity;
import com.moviting.android.util.MyGoogleSignInOptions;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private ListView accountTabList;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    
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

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult);
                        Toast.makeText(getActivity(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, MyGoogleSignInOptions.getGSO())
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        accountTabList = (ListView) view.findViewById(R.id.account_tab_list);
        String[] values = new String[] {
                getResources().getString(R.string.my_profile),
                getResources().getString(R.string.movie_ticket_box),
                getResources().getString(R.string.FAQ),
                getResources().getString(R.string.logout)
        };

        createAccountTabList(accountTabList, values);

        accountTabList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        openProfileActivity();
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        signOut();
                        break;
                    default:
                        break;
                }
            }
        });

        return view;
    }

    private void openProfileActivity() {
        startActivity(ProfileActivity.createIntent(getActivity()));
    }


    public void signOut() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                // Id of the provider (ex: google.com)
                String providerId = profile.getProviderId();
                switch (providerId) {
                    case "facebook.com":
                        LoginManager.getInstance().logOut();
                        break;
                    case "google.com":
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {

                                    }
                                });
                        break;
                }
            }
            User.destructUserInstance();
            mAuth.signOut();
        }

        startActivity(LoginActivity.createIntent(getActivity()));
        getActivity().finish();
    }

    public void createAccountTabList(ListView view, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, values);
        view.setAdapter(adapter);
    }
}
