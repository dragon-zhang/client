package com.letmefold.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * @author SuccessZhang
 */
public class MyGridView extends GridView {

    private int mMaxHeight = Integer.MAX_VALUE >> 2;

    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, int maxHeight) {
        super(context);
        mMaxHeight = maxHeight;
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxHeight(int maxHeight) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
