package com.example.gaacompass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GaaPitchView extends View {

    private static final float PITCH_CORNER_RADIUS_DP = 12f;
    private static final float PITCH_BORDER_DP = 3f;
    private static final float LINE_STROKE_DP = 2f;
    private static final float GOAL_WIDTH_RATIO = 0.15f;
    private static final float GOAL_HEIGHT_RATIO = 0.04f;
    private static final float MARKER_RADIUS_DP = 5f;
    private static final float MARKER_BORDER_DP = 1.5f;
    private static final float INNER_PADDING_DP = 8f;

    private final Paint pitchFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pitchBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerScoredPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerMissedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF pitchRect = new RectF();
    private final RectF goalRect = new RectF();
    private final Path arcPath = new Path();

    private float density;
    private List<FreeAttempt> attempts = new ArrayList<>();
    private OnPitchTapListener tapListener;

    private float pitchLeft;
    private float pitchTop;
    private float pitchRight;
    private float pitchBottom;
    private float pitchW;
    private float pitchH;
    private float centerX;
    private float centerY;

    private boolean touchDownInPitch;

    public interface OnPitchTapListener {
        void onPitchTap(float normX, float normY);
    }

    public GaaPitchView(Context context) {
        super(context);
        init(context);
    }

    public GaaPitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GaaPitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        pitchFillPaint.setColor(0xFF3DAD3D);
        pitchFillPaint.setStyle(Paint.Style.FILL);
        pitchBorderPaint.setColor(0xFFFFFFFF);
        pitchBorderPaint.setStyle(Paint.Style.STROKE);
        pitchBorderPaint.setStrokeWidth(PITCH_BORDER_DP * density);
        linePaint.setColor(0xFFFFFFFF);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_STROKE_DP * density);
        markerScoredPaint.setColor(0xFF22C55E);
        markerScoredPaint.setStyle(Paint.Style.FILL);
        markerMissedPaint.setColor(0xFFEF4444);
        markerMissedPaint.setStyle(Paint.Style.FILL);
        markerBorderPaint.setColor(0xFFFFFFFF);
        markerBorderPaint.setStyle(Paint.Style.STROKE);
        markerBorderPaint.setStrokeWidth(MARKER_BORDER_DP * density);
    }

    public void setAttempts(List<FreeAttempt> attempts) {
        this.attempts = attempts != null ? attempts : new ArrayList<>();
        invalidate();
    }

    public void setOnPitchTapListener(OnPitchTapListener listener) {
        this.tapListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = INNER_PADDING_DP * density;
        pitchLeft = padding;
        pitchTop = padding;
        pitchRight = w - padding;
        pitchBottom = h - padding;
        pitchW = Math.max(0f, pitchRight - pitchLeft);
        pitchH = Math.max(0f, pitchBottom - pitchTop);
        centerX = pitchLeft + pitchW / 2f;
        centerY = pitchTop + pitchH / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        pitchRect.set(pitchLeft, pitchTop, pitchRight, pitchBottom);

        float cornerRadius = PITCH_CORNER_RADIUS_DP * density;
        canvas.drawRoundRect(pitchRect, cornerRadius, cornerRadius, pitchFillPaint);
        canvas.drawRoundRect(pitchRect, cornerRadius, cornerRadius, pitchBorderPaint);
        if (pitchW <= 0f || pitchH <= 0f) return;

        float goalMouthW = pitchW * 0.11f;
        float goalMouthHalfW = goalMouthW / 2f;
        float goalPostExtendPx = 18f * density;
        float goalTopY = pitchTop;
        float goalBottomY = pitchBottom;

        float postLeftX = centerX - goalMouthHalfW;
        float postRightX = centerX + goalMouthHalfW;

        canvas.drawLine(postLeftX, goalTopY, postLeftX, goalTopY - goalPostExtendPx, linePaint);
        canvas.drawLine(postRightX, goalTopY, postRightX, goalTopY - goalPostExtendPx, linePaint);
        canvas.drawLine(postLeftX, goalTopY, postRightX, goalTopY, linePaint);

        canvas.drawLine(postLeftX, goalBottomY, postLeftX, goalBottomY + goalPostExtendPx, linePaint);
        canvas.drawLine(postRightX, goalBottomY, postRightX, goalBottomY + goalPostExtendPx, linePaint);
        canvas.drawLine(postLeftX, goalBottomY, postRightX, goalBottomY, linePaint);

        float goalAreaW = pitchW * 0.30f;
        float goalAreaHalfW = goalAreaW / 2f;
        float goalAreaDepth = pitchH * 0.05f;

        RectF topGoalArea = new RectF(centerX - goalAreaHalfW, goalTopY, centerX + goalAreaHalfW, goalTopY + goalAreaDepth);
        canvas.drawRect(topGoalArea, linePaint);
        RectF bottomGoalArea = new RectF(centerX - goalAreaHalfW, goalBottomY - goalAreaDepth, centerX + goalAreaHalfW, goalBottomY);
        canvas.drawRect(bottomGoalArea, linePaint);

        float[] fractions = new float[]{0.09f, 0.14f, 0.31f, 0.44f, 0.50f, 0.56f, 0.69f, 0.86f, 0.91f};
        for (float f : fractions) {
            float y = pitchTop + pitchH * f;
            canvas.drawLine(pitchLeft, y, pitchRight, y, linePaint);
        }

        float arcRadiusX = pitchW * 0.40f;
        RectF topArcRect = new RectF(
                centerX - arcRadiusX,
                goalTopY - arcRadiusX,
                centerX + arcRadiusX,
                goalTopY + arcRadiusX
        );
        float y20mTop = pitchTop + pitchH * 0.14f;
        canvas.save();
        canvas.clipRect(pitchLeft, y20mTop, pitchRight, pitchBottom);
        canvas.drawArc(topArcRect, 0, 180, false, linePaint);
        canvas.restore();

        RectF bottomArcRect = new RectF(
                centerX - arcRadiusX,
                goalBottomY - arcRadiusX,
                centerX + arcRadiusX,
                goalBottomY + arcRadiusX
        );
        float y20mBottom = pitchTop + pitchH * 0.86f;
        canvas.save();
        canvas.clipRect(pitchLeft, pitchTop, pitchRight, y20mBottom);
        canvas.drawArc(bottomArcRect, 180, 180, false, linePaint);
        canvas.restore();

        float mr = MARKER_RADIUS_DP * density;
        for (FreeAttempt a : attempts) {
            float x = pitchLeft + a.normX * pitchW;
            float y = pitchTop + a.normY * pitchH;
            Paint fill = a.scored ? markerScoredPaint : markerMissedPaint;
            canvas.drawCircle(x, y, mr + (MARKER_BORDER_DP * density), markerBorderPaint);
            canvas.drawCircle(x, y, mr, fill);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tapListener == null) return super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();
        boolean inBounds = (x >= pitchLeft && x <= pitchLeft + pitchW && y >= pitchTop && y <= pitchTop + pitchH);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownInPitch = inBounds;
                if (touchDownInPitch) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                if (touchDownInPitch && inBounds) {
                    float normX = (x - pitchLeft) / pitchW;
                    float normY = (y - pitchTop) / pitchH;
                    normX = Math.max(0f, Math.min(1f, normX));
                    normY = Math.max(0f, Math.min(1f, normY));
                    touchDownInPitch = false;
                    tapListener.onPitchTap(normX, normY);
                    return true;
                }
                touchDownInPitch = false;
                return false;
            case MotionEvent.ACTION_CANCEL:
                touchDownInPitch = false;
                return false;
            default:
                return super.onTouchEvent(event);
        }
    }
}
