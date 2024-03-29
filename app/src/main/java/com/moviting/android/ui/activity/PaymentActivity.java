package com.moviting.android.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;

public class PaymentActivity extends BaseActivity {

    private static final int REQUEST_CREDIT = 0;
    private static final int REQUEST_COUPON = 1;
    private static final int PAYMENT_SUCCESS = 101;
    private static final int PRICE = 20000;
    private static final int PAYMENT_MODE = 1;
    private static final String TAG = "PaymentActivity";
    private String couponUid = "";
    private MatchInfo matchInfo;

    private TextView creditOrCouponAmount;
    private int credit_or_coupon_amount = 0;
    private TextView totalAmount;

    private Button payButton;
    private Button requestPaymentButton;
    private RelativeLayout creditButton;
    private RelativeLayout couponButton;
    private ColorStateList oldColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        creditOrCouponAmount = (TextView) findViewById(R.id.credit_or_coupon_amount);
        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount));
        oldColors = creditOrCouponAmount.getTextColors();

        totalAmount = (TextView)  findViewById(R.id.total_amount);
        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));

        requestPaymentButton = (Button) findViewById(R.id.request_check_payment);
        requestPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(WebViewActivity.createIntent(PaymentActivity.this, "http://plus.kakao.com/home/@%EC%97%B0%EC%8B%9C%EC%98%81"));
            }
        });

        creditButton = (RelativeLayout) findViewById(R.id.credit_button);
        creditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(CreditActivity.createIntent(getBaseContext()), REQUEST_CREDIT);
            }
        });

        couponButton = (RelativeLayout) findViewById(R.id.coupon_button);
        couponButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !matchInfo.myGender.equals(matchInfo.opponentGender) ) {
                    startActivityForResult(CouponActivity.createIntent(getBaseContext(), PAYMENT_MODE), REQUEST_COUPON);
                } else {
                    Toast.makeText(getBaseContext(), "쿠폰은 이성과의 만남에서만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        payButton = (Button) findViewById(R.id.pay_button);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validate()) {
                    updateCreditAndCoupon();
                }
            }
        });

        matchInfo = (MatchInfo)getIntent().getSerializableExtra("matchInfo");
    }

    public static Intent createIntent(Context context, MatchInfo matchInfo) {
        Intent in = new Intent();
        in.setClass(context, PaymentActivity.class);
        in.putExtra("matchInfo", matchInfo);
        return in;
    }

    private void updateCreditAndCoupon() {
        if(couponUid.equals("")){
            getFirebaseDatabaseReference().child("user_point").child(getUid()).setValue(0, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    updateUserPayment("cash");
                }
            });
        } else {
            getFirebaseDatabaseReference().child("user_coupon").child(getUid()).child(couponUid).child("used").setValue(true, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    updateUserPayment("coupon");
                }
            });
        }
    }

    private void updateUserPayment(String paymentType) {
        getFirebaseDatabaseReference().child("match_member_payment").child(matchInfo.matchUid).child(getUid()).child("payment").setValue(true);
        getFirebaseDatabaseReference().child("match_member_payment").child(matchInfo.matchUid).child(getUid()).child("type").setValue(paymentType, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                setResult(PAYMENT_SUCCESS);
                finish();
            }
        });
    }

    private boolean validate(){
        if(credit_or_coupon_amount == PRICE ){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                PaymentActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CREDIT:
                    String usedCredit = data.getStringExtra("usedCredit");
                    if(!usedCredit.equals("0")) {
                        credit_or_coupon_amount = Integer.valueOf(usedCredit);
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount));
                        creditOrCouponAmount.setTextColor(Color.RED);

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(true);
                        payButton.setTextColor(Color.WHITE);
                        couponButton.setEnabled(false);
                    } else {
                        credit_or_coupon_amount = 0;
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount));
                        creditOrCouponAmount.setTextColor(oldColors);

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(false);
                        payButton.setTextColor(Color.LTGRAY);
                        couponButton.setEnabled(true);
                    }
                    break;
                case REQUEST_COUPON:
                    String usedCoupon = data.getStringExtra("usedCoupon");
                    couponUid = data.getStringExtra("couponUid");
                    if(!usedCoupon.equals("0")) {
                        credit_or_coupon_amount = Integer.valueOf(usedCoupon);
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount));
                        creditOrCouponAmount.setTextColor(Color.RED);

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(true);
                        payButton.setTextColor(Color.WHITE);
                        creditButton.setEnabled(false);
                    } else {
                        credit_or_coupon_amount = 0;
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount));
                        creditOrCouponAmount.setTextColor(oldColors);

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(false);
                        payButton.setTextColor(Color.LTGRAY);
                        creditButton.setEnabled(true);
                    }
                    break;
            }
        }
    }
}
