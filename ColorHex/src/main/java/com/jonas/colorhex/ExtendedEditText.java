package com.jonas.colorhex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.EditText;

@SuppressLint("NewApi")
public class ExtendedEditText extends EditText {
    // Stuff to do with our rendering
    TextPaint mTextPaint = new TextPaint();
    float mFontHeight;
    TagDrawable left;

    // The actual suffix
    String mSuffix = "";

    // These are used to store details obtained from the EditText's rendering process
    Rect line0bounds = new Rect();
    int mLine0Baseline;

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        left = new TagDrawable();

        mFontHeight = getTextSize();

        mTextPaint.setColor(getCurrentHintTextColor());
        mTextPaint.setTextSize(mFontHeight);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Setup the left side
        setCompoundDrawablesRelative(left, null, null, null);
    }

    @Override
    public void setTypeface(Typeface typeface) {
        super.setTypeface(typeface);
        if (mTextPaint != null) {
            // Sometimes TextView itself calls me when i'm naked
            mTextPaint.setTypeface(typeface);
        }

        postInvalidate();
    }

    public void setPrefix(String s) {
        left.setText(s);
        setCompoundDrawablesRelative(left, null, null, null);
    }

    public void setSuffix(String s) {
        mSuffix = s;
        setCompoundDrawablesRelative(left, null, null, null);
    }

    @Override
    public void onDraw(Canvas c) {
        mLine0Baseline = getLineBounds(0, line0bounds);

        super.onDraw(c);

        // Now we can calculate what we need!
        int xSuffix = (int) mTextPaint.measureText(left.text + getText().toString()) + getPaddingLeft();

        // We need to draw this like this because
        // setting a right drawable doesn't work properly and we want this
        // just after the text we are editing (but untouchable)
        c.drawText(mSuffix, xSuffix, line0bounds.bottom, mTextPaint);

    }

    // This is for the prefix.
    // It is a drawable for rendering text
    public class TagDrawable extends Drawable {

        public String text = "";

        public void setText(String s) {
            text = s;

            // Tell it we need to be as big as we want to be!
            setBounds(0, 0, getIntrinsicWidth(), getIntrinsicHeight());

            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            // I don't know why this y works here, but it does :)
            // (aka if you are from Google/are Jake Wharton and I have done it wrong, please tell me!)
            canvas.drawText(text, 0, mLine0Baseline + canvas.getClipBounds().top, mTextPaint);
        }

        @Override
        public void setAlpha(int i) {
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return 1;
        }

        @Override
        public int getIntrinsicHeight() {
            return (int) mFontHeight;
        }

        @Override
        public int getIntrinsicWidth() {
            return (int) mTextPaint.measureText(text);
        }
    }

}
