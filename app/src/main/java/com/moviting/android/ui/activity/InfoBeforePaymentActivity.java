package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.moviting.android.R;
import com.moviting.android.model.MatchInfo;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

public class InfoBeforePaymentActivity extends BaseActivity {

    private static final int NUMBER_OF_PAGES = 3;
    private static final int REQUEST_PAYMENT = 100;
    private static final int PAYMENT_SUCCESS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_before_payment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button paymentButton = (Button) findViewById(R.id.payment_button);
        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(PaymentActivity.createIntent(getBaseContext(), getIntent().getStringExtra("matchUid")), REQUEST_PAYMENT);
            }
        });

        CarouselView carouselView = (CarouselView) findViewById(R.id.carouselView);
        carouselView.setPageCount(NUMBER_OF_PAGES);
        carouselView.setViewListener(new ViewListener() {
            @Override
            public View setViewForPosition(int position) {
                View customView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                TextView textView = (TextView) customView.findViewById(android.R.id.text1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                textView.setText(getResources().getTextArray(R.array.info_contents)[position]);
                return customView;
            }
        });
    }

    public static Intent createIntent(Context context, String matchUid) {
        Intent in = new Intent();
        in.setClass(context, InfoBeforePaymentActivity.class);
        in.putExtra("matchUid", matchUid);
        return in;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PAYMENT) {
            if (resultCode == PAYMENT_SUCCESS) {
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                InfoBeforePaymentActivity.this.onBackPressed();
                break;
        }
        return true;
    }
}
