package com.heng.record.video.view.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;

/**
 * 自适应的SurfaceView
 */
public class AutoFitSurfaceView extends SurfaceView {
    public static final int MODE_FITXY = 0;
    public static final int MODE_INSIDE = 1;
    public static final int MODE_OUTSIDE = 2;
    private double targetAspect = -1;
    private int aspectMode = MODE_OUTSIDE;
    public  int originWidth = 0;
    public  int originHeight = 0;
    private int finalWidth;
    private int finalHeight;

    public AutoFitSurfaceView(Context context) {
        this(context, null);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                originWidth = getWidth();
                originHeight = getHeight();
            }
        });
    }

    /**
     * @param mode {@link #MODE_FITXY},{@link #MODE_INSIDE},{@link #MODE_OUTSIDE}
     * @param aspectRatio width/height
     */
    public void setAspectRatio(int mode, double aspectRatio) {

        if (mode != MODE_INSIDE && mode != MODE_OUTSIDE && mode != MODE_FITXY) {
            throw new IllegalArgumentException("illegal mode");
        }
        if (aspectRatio < 0) {
            throw new IllegalArgumentException("illegal aspect ratio");
        }
        if (targetAspect != aspectRatio || aspectMode != mode) {
            targetAspect = aspectRatio;
            aspectMode = mode;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (targetAspect > 0) {
            int initialWidth = originWidth;
            int initialHeight = originHeight;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDiff = targetAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) > 0.01 && aspectMode != MODE_FITXY) {
                if (aspectMode == MODE_INSIDE) {
                    if (aspectDiff > 0) {
                        initialHeight = (int) (initialWidth / targetAspect);
                    } else {
                        initialWidth = (int) (initialHeight * targetAspect);
                    }
                } else if (aspectMode == MODE_OUTSIDE) {
                    if (aspectDiff > 0) {
                        initialWidth = (int) (initialHeight * targetAspect);
                    } else {
                        initialHeight = (int) (initialWidth / targetAspect);
                    }
                }
                Log.e("--------", "  initialWidth:" + initialWidth + "  initialHeight:" + initialHeight);
                getHolder().setFixedSize(initialWidth, initialHeight);
                finalWidth = initialWidth;
                finalHeight = initialHeight;
                setMeasuredDimension(initialWidth, initialHeight);
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        if (finalWidth != 0 && finalHeight != 0) {
            l -= (finalWidth - originWidth) / 2;
            t -= (finalHeight - originHeight) / 2;
            super.layout(l, t, l + finalWidth, t + finalHeight);
        } else {
            super.layout(l, t, r, b);
        }
    }

    /**
     * 由于在MODE_OUTSIDE 模式时，预览图的实际大小可能大于控件大小，故在截取图片时，要做偏移
     */
    public Rect getRealRect(Rect bitmapRect) {
        if (bitmapRect == null || aspectMode != MODE_OUTSIDE) return bitmapRect;
        Rect result = new Rect(bitmapRect);
        if (getWidth() != originWidth) {
            result.left -= getLeft();
            result.right -= getLeft();
        } else if (getHeight() != originHeight) {
            result.top -= getTop();
            result.bottom -= getTop();
        }
        return result;
    }
}
