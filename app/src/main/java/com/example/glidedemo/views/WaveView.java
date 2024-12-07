package com.example.glidedemo.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveView extends View {

    private float mInitialRadius = 0f; // 初始波纹半径
    private float mMaxRadiusRate = 0.85f; // 最大半径与视图的比例
    private float mMaxRadius; // 最大波纹半径
    private long mDuration = 2000; // 每个波纹的持续时间
    private int mSpeed = 500; // 波纹生成速度，每隔多少毫秒生成一个
    private Interpolator mInterpolator = new LinearInterpolator(); // 插值器
    private List<Circle> mCircleList = new ArrayList<>(); // 活跃的波纹列表
    private boolean mIsRunning = false; // 是否正在运行
    private boolean mMaxRadiusSet = false; // 是否手动设置最大半径
    private Paint mPaint; // 画笔
    private long mLastCreateTime; // 上一次波纹生成时间

    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mSpeed);
            }
        }
    };

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
    }

    public void setStyle(Paint.Style style) {
        mPaint.setStyle(style);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void setInitialRadius(float radius) {
        mInitialRadius = radius;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public void setMaxRadius(float maxRadius) {
        this.mMaxRadius = maxRadius;
        mMaxRadiusSet = true;
    }

    public void setMaxRadiusRate(float maxRadiusRate) {
        this.mMaxRadiusRate = maxRadiusRate;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        if (mInterpolator == null) {
            mInterpolator = new LinearInterpolator();
        }
    }

    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    public void stop() {
        mIsRunning = false;
        mCircleList.clear();
        invalidate(); // 停止后刷新界面
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            mPaint.setAlpha(circle.getAlpha());
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, circle.getCurrentRadius(), mPaint);
        }

        if (mIsRunning && mCircleList.size() > 0) {
            invalidate();
        }
    }

    private void newCircle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mSpeed) {
            return;
        }
        Circle circle = new Circle();
        mCircleList.add(circle);
        mLastCreateTime = currentTime;
    }

    private class Circle {
        private float mCurrentRadius; // 当前半径
        private int mAlpha;          // 当前透明度
        private ValueAnimator mAnimator;

        public Circle() {
            mAnimator = ValueAnimator.ofFloat(mInitialRadius, mMaxRadius);
            mAnimator.setDuration(mDuration);
            mAnimator.setInterpolator(mInterpolator);
            mAnimator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                mCurrentRadius = (float) animation.getAnimatedValue();
                mAlpha = (int) ((1.0f - fraction) * 255);
                invalidate();
            });
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCircleList.remove(Circle.this);
                }
            });
            mAnimator.start();
        }

        public float getCurrentRadius() {
            return mCurrentRadius;
        }

        public int getAlpha() {
            return mAlpha;
        }
    }
}

