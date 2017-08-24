/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TridentConfig;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.ease.Linear;
import org.pushingpixels.trident.ease.Sine;
import org.pushingpixels.trident.interpolator.PropertyInterpolator;
import org.pushingpixels.trident.swing.SwingRepaintTimeline;

/**
 *
 * @author Marco
 */
public class CircularWait extends javax.swing.JPanel {
    private static final int ROTATION_ANIMATOR_DURATION = 2000 * 2;
    private static final int SWEEP_ANIMATOR_DURATION = 600 * 2;
    private static final int END_ANIMATOR_DURATION = 200 * 2;

    private Rectangle fBounds = new Rectangle(10, 10, 50, 50);

    private Timeline mRotationAnimator;
    private Timeline mSweepAppearingAnimator;
    private Timeline mSweepDisappearingAnimator;

    private boolean mModeAppearing;
    private boolean mRunning;
    private int mCurrentColor;
    private int mCurrentIndexColor;
    private float mCurrentSweepAngle;
    private float mCurrentRotationAngleOffset = 0;
    private float mCurrentRotationAngle = 0;
    private float mCurrentEndRatio = 1f;

    private float mBorderWidth;
    private int[] mColors;
    private float mSweepSpeed;
    private float mRotationSpeed;
    private int mMinSweepAngle;
    private int mMaxSweepAngle;
    private boolean mFirstSweepAnimation;

    private Integer angleInterpolator = 0;
    private Integer sweepAppearingInterpolator = 0;
    private Integer sweepDisappearingInterpolator = 0;

    private void setCurrentRotationAngle(float angle) {
        mCurrentRotationAngle = angle;
    }

    public CircularWait() {
        mBorderWidth = 4;
        mCurrentIndexColor = 0;
        mSweepSpeed = 1f;
        mRotationSpeed = 1f;
        mMinSweepAngle = 10;
        mMaxSweepAngle = 330;
        
        TridentConfig.getInstance().addPropertyInterpolator(new PropertyInterpolator<Integer>() {
            
            public Class getBasePropertyClass() {
                return Integer.class;
            }

            
            public Integer interpolate(Integer from, Integer to, float timelinePosition) {
                return from;
            }
        });

        setupAnimations();
        new SwingRepaintTimeline(this).playLoop(Timeline.RepeatBehavior.LOOP);        
    }

    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        fBounds = g.getClipBounds();
        
        float startAngle = mCurrentRotationAngle - mCurrentRotationAngleOffset;
        float sweepAngle = mCurrentSweepAngle;
        if (!mModeAppearing) {
            startAngle = startAngle + (360 - sweepAngle);
        }
        startAngle %= 360;
        if (mCurrentEndRatio < 1f) {
            float newSweepAngle = sweepAngle * mCurrentEndRatio;
            startAngle = (startAngle + (sweepAngle - newSweepAngle)) % 360;
            sweepAngle = newSweepAngle;
        }
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(new Color(67, 135, 239));
        g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(fBounds.width / 2 - 20, fBounds.height / 2 - 20, 40, 40, (int) startAngle, (int) sweepAngle);
    }

    public void setCurrentSweepAngle(float currentSweepAngle) {
        mCurrentSweepAngle = currentSweepAngle;
    }

    public float getCurrentSweepAngle() {
        return mCurrentSweepAngle;
    }

    private void setDisappearing() {
        mModeAppearing = false;
        mCurrentRotationAngleOffset = mCurrentRotationAngleOffset + (360 - mMaxSweepAngle);
    }

    private void setAppearing() {
        mModeAppearing = true;
        mCurrentRotationAngleOffset += mMinSweepAngle;
    }

    private void setupAnimations() {
        mRotationAnimator = new Timeline(this);
        mRotationAnimator.addPropertyToInterpolate("angleInterpolator", 0, 360);
        mRotationAnimator.setDuration((long) (ROTATION_ANIMATOR_DURATION / mRotationSpeed));
        mRotationAnimator.setEase(new Linear());        
        mRotationAnimator.setDuration(SWEEP_ANIMATOR_DURATION);
        mRotationAnimator.addCallback(new TimelineCallback() {
            
            public void onTimelineStateChanged(Timeline.TimelineState oldState, Timeline.TimelineState newState, float durationFraction, float timelinePosition) {
            }

            
            public void onTimelinePulse(float durationFraction, float timelinePosition) {
                float angle = durationFraction * 360f;
                setCurrentRotationAngle(angle);
            }
        });

        mRotationAnimator.playLoop(Timeline.RepeatBehavior.LOOP);

        mSweepAppearingAnimator = new Timeline(this);
        mSweepAppearingAnimator.addPropertyToInterpolate("sweepAppearingInterpolator", mMinSweepAngle, mMaxSweepAngle);
        mSweepAppearingAnimator.setDuration((long) (SWEEP_ANIMATOR_DURATION / mSweepSpeed));
//        mSweepAppearingAnimator.setEase(new Spline(0.5f));
        mSweepAppearingAnimator.setEase(new Sine());
        mSweepAppearingAnimator.addCallback(new TimelineCallback() {
            
            public void onTimelineStateChanged(Timeline.TimelineState oldState, Timeline.TimelineState newState, float durationFraction, float timelinePosition) {
                if (oldState == Timeline.TimelineState.PLAYING_FORWARD && newState == Timeline.TimelineState.DONE) {
                    mFirstSweepAnimation = false;
                    setDisappearing();
                    mSweepDisappearingAnimator.play();
                }
            }

            
            public void onTimelinePulse(float durationFraction, float timelinePosition) {
                float animatedFraction = durationFraction;
                float angle;
                if (mFirstSweepAnimation) {
                    angle = animatedFraction * mMaxSweepAngle;
                } else {
                    angle = mMinSweepAngle + animatedFraction * (mMaxSweepAngle - mMinSweepAngle);
                }
                setCurrentSweepAngle(angle);
            }

        });

        mModeAppearing = true;
        mSweepAppearingAnimator.play();

        mSweepDisappearingAnimator = new Timeline(this);
        mSweepDisappearingAnimator.addPropertyToInterpolate("sweepDisappearingInterpolator", mMinSweepAngle, mMaxSweepAngle);
        mSweepDisappearingAnimator.setDuration((long) (SWEEP_ANIMATOR_DURATION / mSweepSpeed));
//        mSweepDisappearingAnimator.setEase(new Spline(0.5f));
        mSweepDisappearingAnimator.setEase(new Sine());
        mSweepDisappearingAnimator.addCallback(new TimelineCallback() {
            
            public void onTimelineStateChanged(Timeline.TimelineState oldState, Timeline.TimelineState newState, float durationFraction, float timelinePosition) {
                if (oldState == Timeline.TimelineState.PLAYING_FORWARD && newState == Timeline.TimelineState.DONE) {
                    setAppearing();
                    mSweepAppearingAnimator.play();
                }
            }

            
            public void onTimelinePulse(float durationFraction, float timelinePosition) {
                float animatedFraction = durationFraction;
                setCurrentSweepAngle(mMaxSweepAngle - animatedFraction * (mMaxSweepAngle - mMinSweepAngle));
            }

        });

    }

    public Integer getAngleInterpolator() {
        return angleInterpolator;
    }

    public void setAngleInterpolator(Integer angleInterpolator) {
        this.angleInterpolator = angleInterpolator;
    }

    public Integer getSweepAppearingInterpolator() {
        return sweepAppearingInterpolator;
    }

    public void setSweepAppearingInterpolator(Integer sweepAppearingInterpolator) {
        this.sweepAppearingInterpolator = sweepAppearingInterpolator;
    }

    public Integer getSweepDisappearingInterpolator() {
        return sweepDisappearingInterpolator;
    }

    public void setSweepDisappearingInterpolator(Integer sweepDisappearingInterpolator) {
        this.sweepDisappearingInterpolator = sweepDisappearingInterpolator;
    }
}
