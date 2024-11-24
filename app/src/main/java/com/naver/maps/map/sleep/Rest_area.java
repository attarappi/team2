package com.naver.maps.map.sleep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.google.android.material.snackbar.Snackbar;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.sleep.vision.CameraSourcePreview;
import com.naver.maps.map.sleep.vision.FaceTracker;
import com.naver.maps.map.sleep.vision.GraphicOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.tomer.fadingtextview.FadingTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.naver.maps.map.sleep.vision.FaceTracker;

public class Rest_area extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private static final int STORAGE_PERMISSION_CODE = 100;

    private MapView mapView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Marker> markers;
    private TextView tvElapsedTime;
    private TextView warningLabel; // 추가된 부분
    private int elapsedHours = 0;   // 경과 시간(시간)
    private int elapsedMinutes = 0; // 경과 시간(분)
    private int elapsedSeconds = 0; // 경과 시간(초)
    private Handler handler = new Handler();
    private Handler warningHandler = new Handler(); // 추가된 부분
    private FadingTextView fadingTextView;
    private Switch mySwitch;
    private SharedPreferences sharedPreferences;
    private final String[] texts = {
            "눈꺼풀 하나의 무게가 생명을 가릅니다. 피곤하다면 쉬어가세요.",
            "피곤할 때는 운전대보다 휴식이 먼저입니다. 쉬는 것 또한 안전 운전입니다.",
            "괜찮겠지'라는 생각, 도로 위에서는 위험 그 자체입니다.",
            "잠깐의 졸음이 길고 긴 후회를 남깁니다. 운전 중 졸리면 잠시라도 쉬어가세요."};
    private int index = 0;


    private static final String TAG = "GooglyEyes";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private boolean mIsFrontFacing = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rest_area);


        //구글 vision api
        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.faceOverlay);

        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if (checkPermissions()) {
            createCameraSource();
        } else {
            requestPermissions();
        }

        // warning label 초기화 및 업데이트 핸들러 설정
        warningLabel = findViewById(R.id.warning_label);
        Runnable updateWarningLabel = new Runnable() {
            @Override
            public void run() {
                // FaceTracker의 warningLevel에 따라 텍스트 업데이트
                switch (FaceTracker.warningLevel) {
                    case 1:
                        warningLabel.setText("경고 조치 1단계");
                        break;
                    case 2:
                        warningLabel.setText("경고 조치 2단계");
                        break;
                    case 3:
                        warningLabel.setText("경고 조치 3단계");
                        break;
                    default:
                        warningLabel.setText("경고 조치 0단계");
                        break;
                }
                warningHandler.postDelayed(this, 100); // 0.1초마다 체크
            }
        };
        warningHandler.post(updateWarningLabel);

        // 위치 권한 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        markers = new ArrayList<>();

        // 맵뷰 초기화
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //경각심 문장 초기화
        fadingTextView = findViewById(R.id.fading_TextView);
        updateFadingTextViewVisibility(); // 초기 상태 반영

        // 첫 번째 텍스트를 즉시 표시
        fadingTextView.setText(texts[index]);
        handler.postDelayed(runnable, 5000);

        tvElapsedTime = findViewById(R.id.tvElapsedTime);
        startTimer();

        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SettingViewActivity로 이동
                Intent intent = new Intent(getApplicationContext(),
                        MusicPlayList.class);
                startActivity(intent);
            }
        });
        Button findNearestButton = findViewById(R.id.find_nearest_button);
        findNearestButton.setOnClickListener(v -> findNearestRestArea());
    }

    private void updateFadingTextViewVisibility() {
        // SharedPreferences에서 Switch 상태 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isSwitchChecked = sharedPreferences.getBoolean("SwitchState", true);
        // Switch 상태에 따라 FadingTextView 가시성 제어
        if (!isSwitchChecked) {
            fadingTextView.setVisibility(View.INVISIBLE); // 안 보이게 설정
        } else {
            fadingTextView.setVisibility(View.VISIBLE); // 보이게 설정
        }
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 위치 소스 설정
        naverMap.setLocationSource(locationSource);

        // 현재 위치 버튼 활성화
        naverMap.getUiSettings().setLocationButtonEnabled(true);

        // 위치 추적 모드 설정
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        // 현재 위치로 카메라 이동
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // 현재 위치로 카메라 이동
                            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(
                                            new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoomTo(10)
                                    .animate(CameraAnimation.Easing);
                            naverMap.moveCamera(cameraUpdate);
                        }
                    });
        }
        // CSV 파일에서 졸음쉼터 데이터 로드 및 마커 표시
        loadRestAreasFromCSV();
    }

    private void loadRestAreasFromCSV() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("rest_area.csv"))
            );

            // 첫 줄 건너뛰기
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2) {
                    try {
                        final double lat = Double.parseDouble(tokens[0]);
                        final double lng = Double.parseDouble(tokens[1]);

                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(lat, lng));
                        marker.setMap(naverMap);

                        marker.setOnClickListener(overlay -> {
                            if (ActivityCompat.checkSelfPermission(
                                    Rest_area.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(Rest_area.this, location -> {
                                            if (location != null) {
                                                showPath(location, new LatLng(lat, lng));
                                            } else {
                                                Toast.makeText(Rest_area.this,
                                                        "현재 위치를 가져올 수 없습니다.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                ActivityCompat.requestPermissions(Rest_area.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST_CODE);
                            }
                            return true;
                        });

                        markers.add(marker);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 두 지점 간의 거리를 계산하는 메소드
    public double calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;

        // Haversine 공식을 사용한 거리 계산
        final int R = 6371; // 지구의 반경 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 거리(km)
    }

    // 가장 가까운 졸음쉼터를 찾는 메소드
    public void findNearestRestArea() {
        FaceTracker.warningLevel = 0;

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        Marker nearestMarker = null;
                        double shortestDistance = Double.MAX_VALUE;

                        // 모든 마커를 순회하며 가장 가까운 위치 찾기
                        for (Marker marker : markers) {
                            double distance = calculateDistance(
                                    currentLocation,
                                    marker.getPosition()
                            );

                            if (distance < shortestDistance) {
                                shortestDistance = distance;
                                nearestMarker = marker;
                            }
                        }

                        // 가장 가까운 졸음쉼터로 경로 표시
                        if (nearestMarker != null) {
                            showPath(location, nearestMarker.getPosition());
                            Toast.makeText(
                                    this,
                                    String.format("가장 가까운 졸음쉼터까지 %.1f km", shortestDistance),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    } else {
                        Toast.makeText(
                                this,
                                "현재 위치를 가져올 수 없습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
    private void showPath(Location currentLocation, LatLng destination) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("nmap://route/car?");

        // 출발지 좌표 설정
        urlBuilder.append("slat=").append(currentLocation.getLatitude());
        urlBuilder.append("&slng=").append(currentLocation.getLongitude());
        urlBuilder.append("&sname=").append("현재위치");

        // 목적지 좌표 설정
        urlBuilder.append("&dlat=").append(destination.latitude);
        urlBuilder.append("&dlng=").append(destination.longitude);
        urlBuilder.append("&dname=").append("졸음쉼터");

        // 앱 식별자 설정
        urlBuilder.append("&appname=").append(getPackageName());

        // Intent로 네이버 지도 앱 실행 가능 여부 확인
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlBuilder.toString()));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        // 네이버 지도 앱 실행 가능한지 확인
        if (intent.resolveActivity(getPackageManager()) != null) {
            // 네이버 지도 앱이 설치된 경우 경로 안내 Intent 실행
            startActivity(intent);
        } else {
            // 네이버 지도 앱이 설치되어 있지 않은 경우, Play 스토어로 이동
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(Uri.parse("market://details?id=com.nhn.android.nmap"));
            startActivity(marketIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // 위치 권한 요청 처리
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
                if (!locationSource.isActivated()) {
                    naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                }
                return;
            }
        }
        // 카메라 권한 요청 처리
        else if (requestCode == RC_HANDLE_CAMERA_PERM) {
            if (grantResults.length > 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 카메라 소스를 생성하는 메서드 호출
                createCameraSource();
                return;
            }

            // 권한이 거부된 경우 다이얼로그 표시
            DialogInterface.OnClickListener listener = (dialog, id) -> finish();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Face Tracker sample")
                    .setMessage(R.string.no_camera_permission)
                    .setPositiveButton(R.string.ok, listener)
                    .show();
        } else {
            // 예상하지 못한 권한 요청 코드가 오면 부모 클래스에서 처리
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    private void startTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // 1. 시간 계산
                elapsedSeconds++;
                if (elapsedSeconds == 60) {
                    elapsedSeconds = 0;
                    elapsedMinutes++;
                    if (elapsedMinutes == 60) {
                        elapsedMinutes = 0;
                        elapsedHours++;
                    }
                }

                // 2. TextView 업데이트
                String timeText = String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
                tvElapsedTime.setText(timeText);

                // 3. 1초 후에 다시 실행
                handler.postDelayed(this, 1000); // 1000ms = 1초
            }
        });
    }
    //여기서부터 구글 vision api
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = view -> ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @NonNull
    private FaceDetector createFaceDetector(Context context) {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setTrackingEnabled(true)
                .setProminentFaceOnly(mIsFrontFacing)
                .setMinFaceSize(mIsFrontFacing ? 0.35f : 0.15f)
                .build();

        Detector.Processor<Face> processor;
        if (mIsFrontFacing) {
            Tracker<Face> tracker = new FaceTracker(mGraphicOverlay, context);
            processor = new LargestFaceFocusingProcessor.Builder(detector, tracker).build();
        } else {
            MultiProcessor.Factory<Face> factory = face -> new FaceTracker(mGraphicOverlay, context);
            processor = new MultiProcessor.Builder<>(factory).build();
        }

        detector.setProcessor(processor);

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available");
            Toast.makeText(context, "Face detector not operational", Toast.LENGTH_LONG).show();

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        } else {
            Toast.makeText(context, "Face detector is operational", Toast.LENGTH_SHORT).show();
        }
        return detector;
    }

    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        int facing = mIsFrontFacing ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK;

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                .setRequestedPreviewSize(320, 240)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
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
        handler.removeCallbacksAndMessages(null);

        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
        updateFadingTextViewVisibility(); // 화면 재활성화 시 상태 업데이트

    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

}