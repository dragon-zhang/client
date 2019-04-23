package com.letmefold.activity.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.Config;
import com.letmefold.R;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUIWrapContentListView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author SuccessZhang
 * @date 2019.4.22
 */
public class CommodityShelvesActivity extends AppCompatActivity implements View.OnClickListener {

    private JSONObject userInfo;

    private QMUIRadiusImageView scan;
    private QMUIRoundButton sure;

    private QMUIWrapContentListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_shelves);
        userInfo = JSON.parseObject(getIntent().getStringExtra("user"));
        findView();
        addListener();
        //扫码上货,entity的selled字段置为0
        //todo 接口未完成,user_id查该用户所拥有的实体店信息(包含store_id)
        OkGo.<String>get("http://" + IP_AND_PORT + "/rest/v1/xxx?" + userInfo.get("user_id"))
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.okgo.model.Response<String> response) {
                        Looper.prepare();
                        JSONObject user = JSON.parseObject(response.body());
                        if (user != null) {

                        }
                        Looper.loop();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        Toast.makeText(CommodityShelvesActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addListener() {
        scan.setOnClickListener(this);
        sure.setOnClickListener(this);
        //假数据
        String[] names = {"名称1", "名称2", "名称3", "名称4", "名称5"};
        String[] brands = {"品牌1", "品牌2", "品牌3", "品牌4", "品牌5"};
        String[] msrps = {"10", "20", "30", "40", "50"};
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("name", names[i]);
            map.put("brand", brands[i]);
            map.put("msrp", msrps[i]);
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(CommodityShelvesActivity.this, data,
                R.layout.activity_commodity_shelves_list_item,
                new String[]{"name", "brand", "msrp"},
                new int[]{R.id.name, R.id.brand, R.id.msrp});
        //todo 重写一个adapter
        listView.setAdapter(adapter);
        sure.setText("确认\n上架");
    }

    private void findView() {
        scan = (QMUIRadiusImageView) findViewById(R.id.scan);
        sure = (QMUIRoundButton) findViewById(R.id.sure);
        listView = (QMUIWrapContentListView) findViewById(R.id.list);
    }

    @Override
    public void onClick(View v) {
        if (scan == v) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 申请权限
                ActivityCompat.requestPermissions(CommodityShelvesActivity.this, new String[]{Manifest.permission.CAMERA}, Config.REQ_PERM_CAMERA);
            } else {
                // 二维码扫码
                Intent intent = new Intent(CommodityShelvesActivity.this, CaptureActivity.class);
                startActivityForResult(intent, Config.REQ_QR_CODE);
            }
        } else if (sure == v) {
            //todo 接口未完成,上货
            OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/xxx")
                    .tag(this)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(com.okgo.model.Response<String> response) {
                            Looper.prepare();
                            JSONObject body = JSON.parseObject(response.body());
                            if (body != null) {

                            }
                            Looper.loop();
                        }

                        @Override
                        public void onError(Response<String> response) {
                            JSONObject jsonObject = JSON.parseObject(response.body());
                            Toast.makeText(CommodityShelvesActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
