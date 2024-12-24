package com.unity.myapplication;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-12-24 10
 * Time:39
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class VerticalScrollTextView extends View {
    private TextPaint textPaint;
    private String text = "";
    private int textColor = Color.BLACK;
    private float textSize = 40f;
    private float charWidth;
    private float charHeight;
    private float lineHeight;
    private int charsPerLine;

    private float paragraphSpacing = 20f;

    private List<String> textLines = new ArrayList<>();
    private float scrollYOffset = 0;
    private ValueAnimator animator;
    private int animDuration = 10000;
    private Scroller scroller;
    private boolean isScrolling = false;

    private boolean isScrollingMode = false;

    private Rect rect = new Rect(0, 0, 0, 0);//画一个矩形

    private float totalTextHeight;
    Rect bounds = new Rect();

    private int columns;

    public VerticalScrollTextView(Context context) {
        this(context, null);
    }

    public VerticalScrollTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalScrollTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(dpToPx(textSize));
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        scroller = new Scroller(getContext());

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(animDuration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            scrollYOffset = (progress * (lineHeight * (textLines == null ? 0 : textLines.size()) + paragraphSpacing));
            scrollYOffset = scrollYOffset % ((lineHeight * (textLines == null ? 0 : textLines.size()) + paragraphSpacing));

            Log.d("scrollYOffset", "scrollYOffset===>" + scrollYOffset);

            invalidate();
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (text == null || text.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        calculateTextSize();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height = 0;
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            height = (int) totalTextHeight; // 如果高度没有指定，则使用文本内容的高度
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = (int) totalTextHeight;
        } else {
            height = heightSize;
        }

        int width = 0;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = (int) (charWidth + getPaddingStart() + getPaddingEnd()); // 如果高度没有指定，则使用文本内容的高度
        } else {
            width = (int) (charWidth + getPaddingStart() + getPaddingEnd());
        }
        Log.d("VerticalLayoutTextView", "width: " + width + ", height: " + height + ", totalTextHeight:" + totalTextHeight);
        setMeasuredDimension(width, height);
        isScrollingMode=totalTextHeight > height;
        setScrolling(totalTextHeight > height);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (text == null){
            return;
        }
        calculateTextSize();
    }

    private void calculateTextSize() {
        if (text == null || text.isEmpty()){
            return;
        }

        textPaint.getTextBounds("字", 0, 1, bounds);
        charWidth = bounds.width();
        charHeight = bounds.height();
        lineHeight = charHeight * 1.2f;

        charsPerLine =1; // 每行最多显示字符数
        textLines.clear();
        for (int i = 0; i < text.length(); i ++) {
            int endIndex = Math.min(i + charsPerLine, text.length());
            textLines.add(text.substring(i, endIndex));
        }
        totalTextHeight = lineHeight * textLines.size();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (text == null || text.isEmpty())
            return;
        int width = getWidth();
        int height = getHeight();
        if (!isScrollingMode && !isScrolling) {
            drawTextWithinBounds(canvas, width, height);
        } else {
            scrollTextView(canvas, height, (float) width);

        }
    }

    private void scrollTextView(Canvas canvas, int height, float width) {
        float startY = 0;
        for (int i = 0; i < textLines.size() * 2; i++) {
            String line = textLines.get(i % textLines.size());
            float y;
            if (i < textLines.size()) {
                y = (startY + (lineHeight) * i - scrollYOffset);
            } else {
                y = (startY + (lineHeight) * i + paragraphSpacing - scrollYOffset);
            }
            if (y < -lineHeight || y > height + lineHeight) {
                continue;
            }
            canvas.drawText(line, width / 2, y, textPaint);
        }
    }

    private void drawTextWithinBounds(Canvas canvas, int width, int height) {
        float cell = (float) height / (textLines.size());

        for (int i = 0; i < textLines.size(); i++) {
            String line = textLines.get(i);

            rect.left = 0;
            rect.top = (int) (i * cell);
            rect.right = width;
            rect.bottom = (int) ((i + 1) * cell);

            //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float fontMetricsTop = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
            float fontMetricsBottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

            int baseLineY = (int) (rect.centerY() - fontMetricsTop / 2 - fontMetricsBottom / 2);//基线中间点的y轴计算公式

            canvas.drawText(line, rect.centerX(), baseLineY, textPaint);
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    // 设置文本
    public void setText(String text) {
        if (text == null) {
            this.text = "";
            return;
        }
        this.text = text;
        if (getWidth() > 0 && getHeight() > 0){
            calculateTextSize();
        }
        requestLayout();
        invalidate();
    }

    // 设置文本颜色
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(textColor);
        invalidate();
    }

    // 设置文本字体大小
    public void setTextSize(float size) {
        this.textSize = size;
        textPaint.setTextSize(dpToPx(size));
        if (getWidth() > 0 && getHeight() > 0){
            calculateTextSize();
        }
        requestLayout();
        invalidate();
    }

    // 设置滚动速度
    public void setAnimDuration(int duration) {
        this.animDuration = duration;
        if (animator != null) {
            animator.setDuration(animDuration);
        }
    }

    // 开始滚动
    public void startScroll() {
        if (isScrollingMode){
            if (animator != null && !animator.isStarted()) {
                float scrollDistance = (lineHeight * (textLines == null ? 0 : textLines.size()) + paragraphSpacing);
                float start = scrollYOffset % scrollDistance;
                animator.setFloatValues(start / scrollDistance, (start + scrollDistance) / scrollDistance);
                animator.start();
                isScrolling = true;
            }
        }

    }

    // 停止滚动
    public void stopScroll() {
        if (isScrollingMode){
            if (animator != null && animator.isStarted()) {
                animator.cancel();
                isScrolling = false;
            }
        }
    }

    //设置是否滚动
    public void setScrolling(boolean enable) {
        isScrolling=enable;
        if (isScrolling) {
            startScroll();
        } else {
            stopScroll();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopScroll();
    }

    private float dpToPx(float dp) {
        Resources resources = getContext().getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
