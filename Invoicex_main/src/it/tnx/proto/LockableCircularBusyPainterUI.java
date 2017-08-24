/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.proto;

import com.jhlabs.image.GaussianFilter;
import it.tnx.commons.SwingUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.jdesktop.jxlayer_old.JXLayer;
import org.jdesktop.jxlayer_old.plaf.ext.LockableUI;
import org.jdesktop.swingx.painter.BusyPainter;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.callback.TimelineCallback;
import org.pushingpixels.trident.ease.Linear;

/**
 *
 * @author mceccarelli
 */
public class LockableCircularBusyPainterUI extends LockableUI implements ActionListener {

    private BusyPainter busyPainter;
    private Timer timer;
    private int frameNumber;

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
    private boolean primorender;

    private JXLayer layer = null;

    private void setCurrentRotationAngle(float angle) {
        mCurrentRotationAngle = angle;
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }

    public void stop() {
        try {
            mRotationAnimator.abort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mSweepAppearingAnimator.abort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mSweepDisappearingAnimator.abort();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public LockableCircularBusyPainterUI() {
        mBorderWidth = 4;
        mCurrentIndexColor = 0;
        mSweepSpeed = 1f;
        mRotationSpeed = 1f;
        mMinSweepAngle = 10;
        mMaxSweepAngle = 330;

        setupAnimations();

//        new SwingRepaintTimeline(this.getLayer()).playLoop(Timeline.RepeatBehavior.LOOP);        
//        busyPainter = new BusyPainter() {
//            @Override
//            protected void doPaint(Graphics2D g, Object t, int width, int height) {
//                Rectangle r = new Rectangle(((JComponent) t).getSize().width, ((JComponent) t).getSize().height);
//                g.setColor(new Color(100, 100, 100, 50));
//                g.fill(r);
//                super.doPaint(g, t, width, height);
//            }
//        };
//        busyPainter.setPaintCentered(true);
//        busyPainter.setPointShape(new Ellipse2D.Float(0, 0, 8, 8));
//        busyPainter.setTrajectory(new Ellipse2D.Float(0, 0, 26, 26));
//        timer = new Timer(75, this);
        timer = new Timer(16, this);
    }

    BufferedImage bImage = null;
    GaussianFilter gf = null;
    Boolean aggiuntoListenerPerStop = false;

    @Override
    protected void paintLayer(Graphics2D g2, JXLayer<? extends JComponent> l) {
        try {
            super.paintLayer(g2, l);
        } catch (Exception e) {
        }

        if (l != null && aggiuntoListenerPerStop == false) {
            aggiuntoListenerPerStop = true;
            if (SwingUtils.getParentJInternalFrame(l) instanceof JInternalFrame) {
                ((JInternalFrame) SwingUtils.getParentJInternalFrame(l)).addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosed(InternalFrameEvent e) {
                        super.internalFrameClosed(e); //To change body of generated methods, choose Tools | Templates.
                        LockableCircularBusyPainterUI.this.stop();
                    }
                });
            }
        }

        if (isLocked()) {

//            if (bImage == null) {
//                gf = new GaussianFilter(3);
//                bImage = new BufferedImage(l.getWidth(), l.getHeight(), BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g2bi = bImage.createGraphics();
//                super.paintLayer(g2bi, l);
//                Graphics2D g2bi = (Graphics2D)g2;
//                Rectangle r = new Rectangle(getLayer().getSize().width, getLayer().getSize().height);
//                g2bi.setColor(new Color(255, 255, 255, 70));
//                g2bi.fill(r);
//                gf.filter(bImage, bImage);                
//                fBounds = new Rectangle(l.getWidth() / 2 - 25, l.getHeight() / 2 - 25, 50, 50);
//            }
            fBounds = new Rectangle(l.getWidth() / 2 - 25, l.getHeight() / 2 - 25, 50, 50);
            Rectangle r = new Rectangle(getLayer().getSize().width, getLayer().getSize().height);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fill(r);

//            g2.drawImage(bImage, null, 0, 0);
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

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(new Color(67, 135, 239));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(fBounds.x, fBounds.y, fBounds.width, fBounds.height, (int) startAngle, (int) sweepAngle);
        }

    }

    @Override
    public void setLocked(boolean isLocked) {
        super.setLocked(isLocked);
        if (isLocked) {
            timer.start();
            mRotationAnimator.resume();
            mSweepAppearingAnimator.resume();
            mSweepDisappearingAnimator.resume();
        } else {
            if (timer != null) {
                timer.stop();
            }
            mRotationAnimator.suspend();
            mSweepAppearingAnimator.suspend();
            mSweepDisappearingAnimator.suspend();
            bImage = null;
        }
    }

//    long precms = 0;
    // Change the frame for the busyPainter
    // and mark BusyPainterUI as dirty
    public void actionPerformed(ActionEvent e) {
//        SwingUtils.inEdt(new Runnable() {
//            public void run() {
//                frameNumber = (frameNumber + 1) % 8;
//                busyPainter.setFrame(frameNumber);
//                // this will repaint the layer
        setDirty(true);
//        getLayer().repaint();
//                System.out.println(this + " -> REPAINT");
//        precms = System.currentTimeMillis();

//            }
//        });
    }
}
