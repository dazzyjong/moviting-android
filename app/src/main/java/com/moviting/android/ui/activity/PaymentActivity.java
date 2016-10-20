package com.moviting.android.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private String couponUid = "";
    private MatchInfo matchInfo;

    private TextView creditOrCouponAmount;
    private int credit_or_coupon_amount = 0;
    private TextView totalAmount;

    private TextView discountOfCredit;
    private TextView discountOfCoupon;

    private Button payButton;
    private RelativeLayout creditButton;
    private RelativeLayout couponButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        creditOrCouponAmount = (TextView) findViewById(R.id.credit_or_coupon_amount);
        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount) + getString(R.string.won));

        totalAmount = (TextView)  findViewById(R.id.total_amount);
        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));

        Button requestPaymentButton = (Button) findViewById(R.id.request_check_payment);
        requestPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
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
                // TODO: check same gender
                startActivityForResult(CouponActivity.createIntent(getBaseContext(), PAYMENT_MODE), REQUEST_COUPON);
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

        discountOfCredit = (TextView) findViewById(R.id.discount_of_credit);
        discountOfCoupon = (TextView) findViewById(R.id.discount_of_coupon);
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
                        discountOfCredit.setText("- " + usedCredit);
                        discountOfCredit.setVisibility(View.VISIBLE);

                        credit_or_coupon_amount = Integer.valueOf(usedCredit);
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount) + getString(R.string.won));

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(true);
                        couponButton.setEnabled(false);
                    } else {
                        discountOfCredit.setVisibility(View.GONE);
                        credit_or_coupon_amount = 0;
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount) + getString(R.string.won));

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(false);
                        couponButton.setEnabled(true);
                    }
                    break;
                case REQUEST_COUPON:
                    String usedCoupon = data.getStringExtra("usedCoupon");
                    couponUid = data.getStringExtra("couponUid");
                    if(!usedCoupon.equals("0")) {
                        discountOfCoupon.setText("- " + usedCoupon);
                        discountOfCoupon.setVisibility(View.VISIBLE);

                        credit_or_coupon_amount = Integer.valueOf(usedCoupon);
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount) + getString(R.string.won));

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(true);
                        creditButton.setEnabled(false);
                    } else {
                        discountOfCoupon.setVisibility(View.GONE);
                        credit_or_coupon_amount = 0;
                        creditOrCouponAmount.setText(String.format("%,d", credit_or_coupon_amount) + getString(R.string.won));

                        totalAmount.setText(String.format("%,d", PRICE - credit_or_coupon_amount) + getString(R.string.won));
                        payButton.setEnabled(false);
                        creditButton.setEnabled(true);
                    }
                    break;
            }
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
        builder.setTitle(R.string.request_check_payment);
        builder.setMessage(R.string.request_check_payment_description);
        builder.setCancelable(false);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }
}
