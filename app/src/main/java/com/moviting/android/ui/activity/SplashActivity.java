package com.moviting.android.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import com.moviting.android.R;

import android.os.Handler;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler hd = new Handler();
        hd.postDelayed(new splashhandler() , 1000); // 3초 후에 hd Handler 실행
    }

    private class splashhandler implements Runnable{
        public void run() {
            startActivity(LoginActivity.createIntent(SplashActivity.this));
            SplashActivity.this.finish(); // 로딩페이지 Activity Stack에서 제거
        }
    }

}