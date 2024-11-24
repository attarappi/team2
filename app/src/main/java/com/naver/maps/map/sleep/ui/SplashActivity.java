package com.naver.maps.map.sleep.ui;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.naver.maps.map.sleep.R;
import com.naver.maps.map.sleep.Rest_area;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread timer = new Thread() {

            public void run() {
                try {
                    sleep(1000);// 4 second wait
                } catch (InterruptedException e) {
                    Log.d("ERROR", e.toString());
                    Thread.currentThread().interrupt();
                } finally {
                    applicationInitialize();
                }
            }
        };

        timer.start();
    }

//  Checking either logged in or a new user
    private void applicationInitialize() {
        Intent intent = new Intent(SplashActivity.this, com.naver.maps.map.sleep.Rest_area.class);
        startActivity(intent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }
}
