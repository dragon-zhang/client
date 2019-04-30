package com.letmefold.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.pojo.CardDetail;
import com.letmefold.pojo.LeasedCard;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author SuccessZhang
 */
public class MainListAdapter extends BaseAdapter implements View.OnClickListener {

    private List<CardDetail> data;
    private int mResource;
    private int[] mTo;
    private Context mContext;
    private String userId;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private int index = -1;

    public MainListAdapter(Context mContext,
                           List<CardDetail> data,
                           @LayoutRes int resource,
                           @IdRes int[] to,
                           String userId) {
        this.mContext = mContext;
        this.data = data;
        this.mResource = resource;
        this.mTo = to;
        this.userId = userId;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(mResource, viewGroup, false);
            holder.sname = (TextView) view.findViewById(mTo[0]);
            holder.location = (TextView) view.findViewById(mTo[1]);
            holder.scope = (TextView) view.findViewById(mTo[2]);
            holder.version = (TextView) view.findViewById(mTo[3]);
            holder.grade = (TextView) view.findViewById(mTo[4]);
            holder.time = (TextView) view.findViewById(mTo[5]);
            holder.lease = (QMUIRoundButton) view.findViewById(mTo[6]);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.sname.setText(data.get(i).getSname());
        holder.location.setText(data.get(i).getLocation());
        holder.scope.setText(data.get(i).getScope());
        holder.version.setText(data.get(i).getIssueVersion());
        holder.grade.setText(data.get(i).getGrade());
        holder.time.setText(sdf.format(data.get(i).getIssueTime()));
        holder.lease.setOnClickListener(this);
        index = i;
        //此处需要返回view 不能是view中某一个
        return view;
    }

    @Override
    public void onClick(View v) {
        if (index != -1) {
            CardDetail detail = data.get(index);
            //目前先写死
            LeasedCard rent = new LeasedCard();
            rent.setCardId(detail.getId());
            rent.setType(0);
            rent.setAvailableTimes(1);
            rent.setExpirationDate(null);
            rent.setRent(new BigDecimal(1));
            rent.setTenantId(userId);
            JSONObject json = (JSONObject) JSON.toJSON(rent);
            OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/leasedcard/rent")
                    .tag(this)
                    .upJson(json.toJSONString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(com.okgo.model.Response<String> response) {
                            JSONObject result = JSON.parseObject(response.body());
                            if ("OK".equals(result.getString("msg"))) {
                                Toast.makeText(mContext, "租卡成功", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            JSONObject jsonObject = JSON.parseObject(response.body());
                            Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private class ViewHolder {
        private TextView sname;
        private TextView location;
        private TextView scope;
        private TextView version;
        private TextView grade;
        private TextView time;
        private QMUIRoundButton lease;
    }
}