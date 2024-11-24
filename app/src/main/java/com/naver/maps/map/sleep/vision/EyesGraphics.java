package com.naver.maps.map.sleep.vision;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class EyesGraphics extends GraphicOverlay.Graphic {
    private final Paint mEyeOutlinePaint;
    private PointF mLeftPosition;
    private boolean mLeftOpen;
    private PointF mRightPosition;
    private boolean mRightOpen;

    public EyesGraphics(GraphicOverlay overlay) {
        super(overlay);

        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(Color.RED);
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(5.0f);
    }

    /**
     * 눈 좌표를 업데이트하고 오버레이를 다시 그립니다.
     */
    public void updateEyes(PointF leftPosition, boolean leftOpen, PointF rightPosition, boolean rightOpen) {
        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mLeftPosition != null && !mLeftOpen) {
            drawClosedEye(canvas, mLeftPosition, 0.20f);
        }
        if (mRightPosition != null && !mRightOpen) {
            drawClosedEye(canvas, mRightPosition, 0.20f);
        }
    }

    /**
     * 눈 감김 상태를 사각형으로 표시합니다.
     */
    private void drawClosedEye(Canvas canvas, PointF eyePosition, float proportion) {
        // 좌표 변환
        float transformedX = translateX(eyePosition.x);
        float transformedY = translateY(eyePosition.y);

        // 사각형 경계 계산
        float left = transformedX - proportion * mOverlay.getWidth() / 10;
        float top = transformedY - proportion * mOverlay.getHeight() / 10;
        float right = transformedX + proportion * mOverlay.getWidth() / 10;
        float bottom = transformedY + proportion * mOverlay.getHeight() / 10;

        // 사각형 그리기
        canvas.drawRect(left, top, right, bottom, mEyeOutlinePaint);
    }
}
