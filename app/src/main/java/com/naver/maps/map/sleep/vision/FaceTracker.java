package com.naver.maps.map.sleep.vision;

import android.content.Context;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.HashMap;
import java.util.Map;

import com.naver.maps.map.sleep.R;
import com.naver.maps.map.sleep.Rest_area;

public class FaceTracker extends Tracker<Face> {
    private static final float EYE_CLOSED_THRESHOLD = 0.5f; // 눈 감기 기준값
    private static final int SOUND_DURATION_MS = 5000; // 사운드 재생 시간 (5초)
    public static int warningLevel = 0;

    private final GraphicOverlay mOverlay;
    private EyesGraphics mEyesGraphics;
    private final Context context;

    private MediaPlayer warningSound; // 1단계 경고음 (MP3)
    private MediaPlayer alertSound; // 2단계 경고음 (MP3)
    private MediaPlayer level3Sound; // 3단계 경고음 (MP3)

    private final Handler handler = new Handler(); // 경고음 정지 핸들러
    private Runnable stopRunnable; // 경고음 정지를 위한 Runnable

    private long eyesClosedStartTime = 0; // 눈 감기 시작 시간
    private boolean wasEyesClosed = false; // 이전에 눈이 감겼는지 여부

    private final Map<Integer, PointF> mPreviousProportions = new HashMap<>();

    public FaceTracker(GraphicOverlay overlay, Context context) {
        mOverlay = overlay;
        this.context = context;
        initializeSounds();
    }

    private void initializeSounds() {
        try {
            warningSound = MediaPlayer.create(context, R.raw.warning_sound); // 1단계 MP3
            alertSound = MediaPlayer.create(context, R.raw.alert_sound); // 2단계 MP3
            level3Sound = MediaPlayer.create(context, R.raw.level3_sound); // 3단계 MP3
            Log.d("FaceTracker", "Sounds initialized successfully");
        } catch (Exception e) {
            Log.e("FaceTracker", "Sound initialization error: " + e.getMessage());
        }
    }

    @Override
    public void onNewItem(int id, Face face) {
        mEyesGraphics = new EyesGraphics(mOverlay);
    }

    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mEyesGraphics);

        PointF leftPosition = getLandmarkPosition(face, Landmark.LEFT_EYE);
        PointF rightPosition = getLandmarkPosition(face, Landmark.RIGHT_EYE);

        float leftOpenScore = face.getIsLeftEyeOpenProbability();
        float rightOpenScore = face.getIsRightEyeOpenProbability();

        if (leftOpenScore != Face.UNCOMPUTED_PROBABILITY && rightOpenScore != Face.UNCOMPUTED_PROBABILITY) {
            boolean isLeftOpen = (leftOpenScore > EYE_CLOSED_THRESHOLD);
            boolean isRightOpen = (rightOpenScore > EYE_CLOSED_THRESHOLD);
            boolean areEyesClosed = !isLeftOpen && !isRightOpen;

            if (areEyesClosed) {
                handleEyesClosed();
            } else {
                resetWarningState();
            }

            mEyesGraphics.updateEyes(leftPosition, isLeftOpen, rightPosition, isRightOpen);
        }
    }

    private void handleEyesClosed() {
        if (!wasEyesClosed) {
            eyesClosedStartTime = System.currentTimeMillis();
            wasEyesClosed = true;
        } else {
            long eyesClosedDuration = System.currentTimeMillis() - eyesClosedStartTime;

            if (warningLevel == 0 && eyesClosedDuration >= 3000) {
                playWarningSound();
                warningLevel = 1;
                eyesClosedStartTime = System.currentTimeMillis();
                // updateWarningLabel() 호출 제거
                Log.d("FaceTracker", "Warning Level 1 triggered at 3 seconds");
            } else if (warningLevel == 1 && eyesClosedDuration >= 3000) {
                playAlertSound();
                warningLevel = 2;
                eyesClosedStartTime = System.currentTimeMillis();
                // updateWarningLabel() 호출 제거
                Log.d("FaceTracker", "Warning Level 2 triggered at 3 seconds (after Level 1)");
            } else if (warningLevel == 2 && eyesClosedDuration >= 3000) {
                playLevel3Sound();
                warningLevel = 3;
                eyesClosedStartTime = System.currentTimeMillis();
                // updateWarningLabel() 호출 제거
                Log.d("FaceTracker", "Warning Level 3 triggered at 3 seconds (after Level 2)");
            }
        }
    }

    private void resetWarningState() {
        wasEyesClosed = false;
        eyesClosedStartTime = 0;
        mOverlay.remove(mEyesGraphics);
    }

    private void updateWarningLabel(String text) {
        ((Rest_area) context).runOnUiThread(() -> {
            TextView warningLabel = ((Rest_area) context).findViewById(R.id.warning_label);
            if (warningLabel != null) {
                warningLabel.setText(text);
            }
        });
    }

    private void playWarningSound() {
        stopSounds();
        if (warningSound != null && !warningSound.isPlaying()) {
            warningSound.start();
        }
        scheduleSoundStop();
    }

    private void playAlertSound() {
        stopSounds();
        if (alertSound != null && !alertSound.isPlaying()) {
            alertSound.start();
        }
        scheduleSoundStop();
    }

    private void playLevel3Sound() {
        stopSounds();
        if (level3Sound != null && !level3Sound.isPlaying()) {
            level3Sound.start();
        }
        scheduleSoundStop();
    }

    private void stopSounds() {
        if (warningSound != null && warningSound.isPlaying()) {
            warningSound.stop();
            warningSound.prepareAsync();
        }
        if (alertSound != null && alertSound.isPlaying()) {
            alertSound.stop();
            alertSound.prepareAsync();
        }
        if (level3Sound != null && level3Sound.isPlaying()) {
            level3Sound.stop();
            level3Sound.prepareAsync();
        }
        if (stopRunnable != null) {
            handler.removeCallbacks(stopRunnable);
        }
    }

    private void scheduleSoundStop() {
        stopRunnable = this::stopSounds;
        handler.postDelayed(stopRunnable, SOUND_DURATION_MS);
    }

    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mEyesGraphics);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mEyesGraphics);
        stopSounds();
    }

    private PointF getLandmarkPosition(Face face, int landmarkId) {
        for (Landmark landmark : face.getLandmarks()) {
            if (landmark.getType() == landmarkId) {
                return landmark.getPosition();
            }
        }
        return null;
    }
}
