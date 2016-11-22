package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CouponActivity extends BaseActivity {

    private static final String TAG = "CouponActivity";
    private static final int PAYMENT_MODE = 1;
    private static final int ACCOUNT_MODE = 2;

    private ListView couponList;

    private int mode;

    private class Coupon {
        String uid;
        String kind;
        boolean checked;

        Coupon(String uid, String kind) {
            this.kind = kind;
            this.uid = uid;
            checked = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mode = getIntent().getIntExtra("mode", 1);
        couponList = (ListView) findViewById(R.id.coupon_list);

        getCoupon();

        if(mode == PAYMENT_MODE) {
            couponList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ArrayList<Coupon> couponArray = ((CouponAdapter)couponList.getAdapter()).getCouponArray();

                    if (couponArray.get(i).checked) {
                        couponArray.get(i).checked = false;
                    } else if (!couponArray.get(i).checked) {
                        for (Coupon coupon : couponArray) {
                            coupon.checked = false;
                        }
                        couponArray.get(i).checked = true;
                    }
                    ((CouponAdapter) couponList.getAdapter()).notifyDataSetChanged();
                }
            });
        } else if(mode == ACCOUNT_MODE) {
            getSupportActionBar().setTitle("쿠폰함");
        }

        if(mode == PAYMENT_MODE) {
            Button button = (Button) findViewById(R.id.use_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean chose = false;
                    String uid = "";
                    for (Coupon coupon : ((CouponAdapter)couponList.getAdapter()).getCouponArray()) {
                        if (coupon.checked) {
                            chose = true;
                            uid = coupon.uid;
                        }
                    }
                    Intent intent = new Intent();

                    if (chose) {
                        intent.putExtra("couponUid", uid);
                        intent.putExtra("usedCoupon", "20000");
                    } else {
                        intent.putExtra("usedCoupon", "0");
                    }

                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        } else if(mode == ACCOUNT_MODE) {
            findViewById(R.id.use_button).setVisibility(View.GONE);
        }
    }

    public static Intent createIntent(Context context, int mode) {
        Intent in = new Intent();
        in.putExtra("mode", mode);
        in.setClass(context, CouponActivity.class);
        return in;
    }

    private void getCoupon() {
        getFirebaseDatabaseReference().child("user_coupon").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Coupon> couponArray = new ArrayList<>();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    HashMap<String, Object> object = (HashMap)snapshot.getValue();
                    if(!(boolean)object.get("used")) {
                        couponArray.add(new Coupon(snapshot.getKey(), (String)object.get("kind")));
                    }
                }
                if(mode == PAYMENT_MODE) {
                    CouponAdapter couponAdapter = new CouponAdapter(PAYMENT_MODE, couponArray);
                    couponList.setAdapter(couponAdapter);
                } else if(mode == ACCOUNT_MODE) {
                    CouponAdapter couponAdapter = new CouponAdapter(ACCOUNT_MODE, couponArray);
                    couponList.setAdapter(couponAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                CouponActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    public class CouponAdapter extends BaseAdapter {
        private ArrayList<Coupon> couponArray;
        private int mode;

        CouponAdapter(int mode, ArrayList<Coupon> arrayList) {
            couponArray = arrayList;
            this.mode = mode;
        }

        @Override
        public int getCount() {
            return couponArray.size();
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
            LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(mode == PAYMENT_MODE) {
                PaymentModeHolder paymentModeHolder;
                if(view == null) {
                    paymentModeHolder = new PaymentModeHolder();
                    view = inflater.inflate(R.layout.coupon_item, null);

                    paymentModeHolder.checkedTextView = (CheckedTextView) view.findViewById(R.id.coupon_item);
                    paymentModeHolder.textView = (TextView) view.findViewById(R.id.coupon_name);
                    paymentModeHolder.button = (Button) view.findViewById(R.id.detail);
                    paymentModeHolder.button.setTag(i);
                    paymentModeHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDialog(couponArray.get((Integer)view.getTag()).kind);
                        }
                    });
                    view.setTag(paymentModeHolder);
                } else {
                    paymentModeHolder = (PaymentModeHolder)view.getTag();
                }
                paymentModeHolder.textView.setText(couponArray.get(i).kind);
                paymentModeHolder.checkedTextView.setChecked(couponArray.get(i).checked);
            } else {
                AccountModeHolder accountModeHolder;
                if(view == null) {
                    accountModeHolder = new AccountModeHolder();
                    view = inflater.inflate(R.layout.coupon_item_for_account, null);

                    accountModeHolder.textView = (TextView) view.findViewById(R.id.coupon_label);
                    accountModeHolder.button = (Button) view.findViewById(R.id.detail);
                    accountModeHolder.button.setTag(i);
                    accountModeHolder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDialog(couponArray.get((Integer)view.getTag()).kind);
                        }
                    });
                    view.setTag(accountModeHolder);
                } else {
                    accountModeHolder = (AccountModeHolder)view.getTag();
                }
                accountModeHolder.textView.setText(couponArray.get(i).kind);
            }
            return view;
        }

        public ArrayList<Coupon> getCouponArray() {
            return couponArray;
        }

        class AccountModeHolder {
            TextView textView;
            Button button;
        }

        class PaymentModeHolder {
            CheckedTextView checkedTextView;
            TextView textView;
            Button button;
        }
    }

    private void showDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CouponActivity.this);
        View layout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setTitle(title);
        builder.setView(layout);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }
}
