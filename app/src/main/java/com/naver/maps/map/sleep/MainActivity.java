package com.naver.maps.map.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.naver.maps.map.sleep.ui.SplashActivity;
import com.tomer.fadingtextview.FadingTextView;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView Start_Icon;

    private FadingTextView fadingTextView;
    private final Handler handler = new Handler();
    private final String[] texts = {
            "눈꺼풀 하나의 무게가 생명을 가릅니다. 피곤하다면 쉬어가세요.",
            "피곤할 때는 운전대보다 휴식이 먼저입니다. 쉬는 것 또한 안전 운전입니다.",
            "괜찮겠지’라는 생각, 도로 위에서는 위험 그 자체입니다.",
            "잠깐의 졸음이 길고 긴 후회를 남깁니다. 운전 중 졸리면 잠시라도 쉬어가세요."};
    private int index = 0;

    //앱 실행 화면 출력
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //앱 실행 버튼 클릭 시 메인 화면으로 이동
        Start_Icon = (ImageView) findViewById(R.id.start_icon);
        Start_Icon.setImageResource(R.drawable.start_icon);
        Start_Icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Rest_area.class);
                startActivity(intent);
            }
        });
        fadingTextView = findViewById(R.id.fading_TextView);
        // 첫 번째 텍스트를 즉시 표시
        fadingTextView.setText(texts[index]);
        handler.postDelayed(runnable, 5000);

    }
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            fadingTextView.setText(texts[index]);
            index = (index + 1) % texts.length; // 배열 순환
            handler.postDelayed(this, 5000); // 5초마다 실행
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // 액티비티 종료 시 Handler 중지
    }
}