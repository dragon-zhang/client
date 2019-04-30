package com.letmefold.view;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;

/**
 * 继承自 {@link QMUIPopup}，在 {@link QMUIPopup} 的基础上，支持显示一个列表。
 *
 * @author SuccessZhang
 * @date 2019.4.20
 */
public class MyGridPopup extends QMUIPopup {

    private BaseAdapter mAdapter;

    private int numColumns;

    /**
     * 构造方法。
     *
     * @param context   传入一个 Context。
     * @param direction Popup 的方向，为 {@link QMUIPopup#DIRECTION_NONE}, {@link QMUIPopup#DIRECTION_TOP} 和 {@link QMUIPopup#DIRECTION_BOTTOM} 中的其中一个值。
     * @param adapter   列表的 Adapter
     */
    public MyGridPopup(Context context, @Direction int direction, BaseAdapter adapter, int numColumns) {
        super(context, direction);
        mAdapter = adapter;
        this.numColumns = numColumns;
    }

    public void create(int width, int maxHeight, AdapterView.OnItemClickListener onItemClickListener) {
        GridView gridView = new MyGridView(mContext, maxHeight);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, maxHeight);
        gridView.setLayoutParams(lp);
        gridView.setAdapter(mAdapter);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.setOnItemClickListener(onItemClickListener);
        gridView.setNumColumns(numColumns);
        setContentView(gridView);
    }
}
