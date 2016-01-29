package eu.magisterapp.magisterapp.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import eu.magisterapp.magisterapp.R;

/**
 * Created by max on 1/29/16.
 */
public class FloatingSwipeRefreshLayout extends SwipeRefreshLayout {

    Drawable mForegroundDrawable;

    public FloatingSwipeRefreshLayout(Context context)
    {
        super(context, null);
    }

    public FloatingSwipeRefreshLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingSwipeRefreshLayout, 0, 0);

        mForegroundDrawable = a.getDrawable(R.styleable.FloatingSwipeRefreshLayout_foreground);

        if (mForegroundDrawable != null)
        {
            mForegroundDrawable.setCallback(this);
            setWillNotDraw(false);
        }

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mForegroundDrawable != null)
        {
            mForegroundDrawable.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mForegroundDrawable != null)
        {
            mForegroundDrawable.setBounds(0, 0, w, h);
        }

    }
}
