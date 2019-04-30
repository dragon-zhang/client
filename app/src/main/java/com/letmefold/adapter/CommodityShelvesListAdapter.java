package com.letmefold.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.List;
import java.util.Map;

/**
 * @author SuccessZhang
 */
public class CommodityShelvesListAdapter extends BaseAdapter {

    private List<Map<String, String>> data;
    private int mResource;
    private int[] mTo;
    private Context mContext;

    public CommodityShelvesListAdapter(Context mContext,
                                       List<Map<String, String>> data,
                                       @LayoutRes int resource,
                                       @IdRes int[] to) {
        this.mContext = mContext;
        this.data = data;
        this.mResource = resource;
        this.mTo = to;
    }

    public String getCountPrice() {
        double countPrice = 0;
        for (Map<String, String> map : data) {
            countPrice = countPrice + Double.valueOf(map.get("msrp"));
        }
        return String.valueOf(countPrice);
    }

    /**
     * 返回item的个数
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * 返回每一个item对象
     */
    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    /**
     * 返回每一个item的id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * 暂时不做优化处理，后面会专门整理BaseAdapter的优化
     */
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(mResource, viewGroup, false);
            holder.name = (TextView) view.findViewById(mTo[0]);
            holder.brand = (TextView) view.findViewById(mTo[1]);
            holder.msrp = (TextView) view.findViewById(mTo[2]);
            holder.delete = (QMUIRoundButton) view.findViewById(mTo[3]);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.name.setText(data.get(i).get("name"));
        holder.brand.setText(data.get(i).get("brand"));
        holder.msrp.setText(data.get(i).get("msrp"));
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.remove(i);
                notifyDataSetChanged();
            }
        });
        //此处需要返回view 不能是view中某一个
        return view;
    }

    private class ViewHolder {
        private TextView name;
        private TextView brand;
        private TextView msrp;
        private QMUIRoundButton delete;
    }

}
