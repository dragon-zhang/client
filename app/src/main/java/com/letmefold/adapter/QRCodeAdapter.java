package com.letmefold.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.google.zxing.WriterException;
import com.letmefold.R;
import com.letmefold.utils.QRCodeUtil;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;

import static com.mob.MobSDK.getContext;

/**
 * @author SuccessZhang
 */
public class QRCodeAdapter extends BaseAdapter {

    private String price;
    private int mResource;
    private int[] mTo;
    private Context mContext;
    private String storeId;

    public QRCodeAdapter(Context mContext,
                         String price,
                         @LayoutRes int resource,
                         @IdRes int[] to,
                         String storeId) {
        this.mContext = mContext;
        this.price = price;
        this.mResource = resource;
        this.mTo = to;
        this.storeId = storeId;
    }

    /**
     * 返回item的个数
     */
    @Override
    public int getCount() {
        return 1;
    }

    /**
     * 返回每一个item对象
     */
    @Override
    public Object getItem(int i) {
        return price;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(mResource, viewGroup, false);
            holder.qrCode = (QMUIRadiusImageView) view.findViewById(mTo[0]);
            //holder.barCode = (QMUIRadiusImageView) view.findViewById(R.id.bar_code);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        try {
            //生成二维码
            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.logo_square);
            holder.qrCode.setImageBitmap(QRCodeUtil.createQRCodeBitmapWithImage(storeId + "-" + price, 300, 300, "2",
                    "utf-8", "H",
                    bitmap, 50));
            //生成条形码
            //barCode.setImageBitmap(BarCodeUtil.createBarCodeBitmap("012345678912", 320, 80, true));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        //此处需要返回view 不能是view中某一个
        return view;
    }

    private class ViewHolder {
        private QMUIRadiusImageView qrCode;
        //private QMUIRadiusImageView barCode;
    }
}