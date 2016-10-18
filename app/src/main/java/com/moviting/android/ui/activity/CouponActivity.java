package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class CouponActivity extends BaseActivity {

    private static final String TAG = "CouponActivity";
    private ListView couponList;
    private ArrayList<Coupon> couponArray;

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

        couponList = (ListView) findViewById(R.id.coupon_list);
        couponList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView checkedTextView = (CheckedTextView) view;

                if(checkedTextView.isChecked() && couponArray.get(i).checked){
                    checkedTextView.setChecked(false);
                    couponArray.get(i).checked = false;
                } else if(checkedTextView.isChecked() && !couponArray.get(i).checked) {
                    for(Coupon coupon:couponArray) {
                        coupon.checked = false;
                    }
                    couponArray.get(i).checked = true;
                }
            }
        });

        couponArray = new ArrayList<>();
        getCoupon();

        Button button = (Button) findViewById(R.id.use_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean chose = false;
                String uid = "";
                for(Coupon coupon:couponArray) {
                    if(coupon.checked) {
                        chose = true;
                        uid = coupon.uid;
                    }
                }
                Intent intent = new Intent();

                if(chose) {
                    intent.putExtra("couponUid", uid);
                    intent.putExtra("usedCoupon", "20000");
                } else {
                    intent.putExtra("usedCoupon", "0");
                }

                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, CouponActivity.class);
        return in;
    }

    private void getCoupon() {
        getFirebaseDatabaseReference().child("user_coupon").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    HashMap<String, Object> object = (HashMap)snapshot.getValue();
                    if(!(boolean)object.get("used")) {
                        couponArray.add(new Coupon(snapshot.getKey(), (String)object.get("kind")));
                    }
                }
                createAdapterAndAddItems(couponList, couponArray);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    private void createAdapterAndAddItems(ListView view, ArrayList<Coupon> coupons) {

        String[] values = new String[coupons.size()];

        for(int i = 0; i < coupons.size(); i++) {
            values[i] = coupons.get(i).kind;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.coupon_item, R.id.coupon_item, values);
        view.setAdapter(adapter);

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
}