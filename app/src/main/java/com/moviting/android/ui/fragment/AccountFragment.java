package com.moviting.android.ui.fragment;

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
import com.moviting.android.ui.activity.TicketBoxActivity1;
import com.moviting.android.ui.activity.WebViewActivity;

public class AccountFragment extends BaseFragment {

    private static final String TAG = "AccountFragment";
    private static final int ACCOUNT_MODE = 2;

    private ListView accountTabList;
    private FirebaseAuth mAuth;

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
                getResources().getString(R.string.movie_ticket_box),
                getResources().getString(R.string.coupon_box),
                getResources().getString(R.string.FAQ),
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
                        startActivity(TicketBoxActivity1.createIntent(getActivity()));
                        break;
                    case 2:
                        startActivity(CouponActivity.createIntent(getActivity(), ACCOUNT_MODE));
                        break;
                    case 3:
                        startActivity(WebViewActivity.createIntent(getActivity(), "http://theysy.com/qna.html"));
                        break;
                    case 4:
                        signOut();
                        break;
                    default:
                        break;
                }
            }
        });

        return view;
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
