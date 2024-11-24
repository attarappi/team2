package com.naver.maps.map.sleep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class settingView extends AppCompatActivity {

    ImageView  Image_Music;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_view);


        Image_Music = (ImageView) findViewById(R.id.image_music);
        Image_Music.setImageResource(R.drawable.music_file_btn);
        Image_Music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        MusicPlayList.class);
                startActivity(intent);
            }
        });
    }

}
