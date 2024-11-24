package com.naver.maps.map.sleep;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.media.MediaPlayer;
import android.os.Handler;

public class MusicPlayList extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;

    private RecyclerView recyclerViewMP3;
    private Button btnPlay, btnPause, btnStop;
    private TextView tvMP3, tvTime;
    private EditText searchBar;
    private SeekBar sbMP3;
    private ArrayList<String> mp3List;
    private ArrayList<String> filteredList;
    private String selectedMP3;
    private String mp3Path;
    private MediaPlayer mPlayer;
    private boolean isPaused = false;
    private final Handler handler = new Handler();
    private String currentMP3 = null;
    private MusicAdapter musicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_playlist);

        recyclerViewMP3 = findViewById(R.id.ListViewMP3);
        recyclerViewMP3.setLayoutManager(new LinearLayoutManager(this));

        btnPlay = findViewById(R.id.BtnPlay);
        btnPause = findViewById(R.id.BtnPause);
        btnStop = findViewById(R.id.BtnStop);
        tvMP3 = findViewById(R.id.TvMP3);
        tvTime = findViewById(R.id.TvTime);
        sbMP3 = findViewById(R.id.SbMP3);
        searchBar = findViewById(R.id.SearchBar);

        btnPlay.setOnClickListener(v -> playMusic());
        btnPause.setOnClickListener(v -> pauseMusic());
        btnStop.setOnClickListener(v -> stopMusic());

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "저장소 관리 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            } else {
                loadMP3Files();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                loadMP3Files();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMP3Files();
            } else {
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMP3Files() {
        mp3List = new ArrayList<>();
        filteredList = new ArrayList<>();

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (!dir.exists() || !dir.isDirectory()) {
            Toast.makeText(this, "Download 폴더가 존재하지 않습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".mp3")) {
                    mp3List.add(file.getName());
                }
            }
        }

        if (mp3List.isEmpty()) {
            Toast.makeText(this, "MP3 파일이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            filteredList.addAll(mp3List);
            musicAdapter = new MusicAdapter(filteredList, new MusicAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String fileName, int position) {
                    selectedMP3 = fileName;
                    playMusic();
                }

                @Override
                public void onDelete(String fileName, int position) {
                    filteredList.remove(position);
                    musicAdapter.notifyItemRemoved(position);
                    Toast.makeText(MusicPlayList.this, fileName + " 삭제됨", Toast.LENGTH_SHORT).show();
                }
            });

            recyclerViewMP3.setAdapter(musicAdapter);
        }
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(mp3List);
        } else {
            for (String item : mp3List) {
                if (item.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        musicAdapter.notifyDataSetChanged();
    }

    private void playMusic() {
        if (selectedMP3 == null) {
            Toast.makeText(this, "재생할 곡을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mPlayer != null) {
                if (!selectedMP3.equals(currentMP3)) {
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                    isPaused = false;
                }
            }

            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
                String fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + selectedMP3;
                mPlayer.setDataSource(fullPath);
                mPlayer.prepare();
                mPlayer.start();

                currentMP3 = selectedMP3;

                tvMP3.setText("실행중인 음악 : " + selectedMP3);
                tvTime.setText("진행시간 : 00:00 / " + formatTime(mPlayer.getDuration()));
                sbMP3.setMax(100);
                sbMP3.setProgress(0);
                updateSeekBar();
            } else if (isPaused) {
                mPlayer.start();
                isPaused = false;
            }
        } catch (IOException e) {
            Toast.makeText(this, "파일을 재생할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseMusic() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            isPaused = true;
            tvMP3.setText("실행중인 음악 : " + currentMP3 + " (일시중지)");
        } else {
            Toast.makeText(this, "재생 중인 음악이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopMusic() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying() || isPaused) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
                isPaused = false;

                currentMP3 = null;

                tvMP3.setText("실행중인 음악 : 없음");
                tvTime.setText("진행시간 : 00:00 / 00:00");
                sbMP3.setProgress(0);
            } else {
                Toast.makeText(this, "재생 중인 음악이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "재생 중인 음악이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSeekBar() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            int currentPosition = mPlayer.getCurrentPosition();
            int totalDuration = mPlayer.getDuration();

            sbMP3.setProgress((int) ((currentPosition / (float) totalDuration) * sbMP3.getMax()));
            tvTime.setText("진행시간 : " + formatTime(currentPosition) + " / " + formatTime(totalDuration));
        }
        handler.postDelayed(this::updateSeekBar, 1000);
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMusic(); // 화면을 나갈 때 음악 중지
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }
}
