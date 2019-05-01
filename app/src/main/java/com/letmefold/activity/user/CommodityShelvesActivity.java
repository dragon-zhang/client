package com.letmefold.activity.user;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.Config;
import com.letmefold.R;
import com.letmefold.adapter.CommodityShelvesListAdapter;
import com.letmefold.adapter.QRCodeAdapter;
import com.letmefold.pojo.Store;
import com.letmefold.pojo.User;
import com.letmefold.view.MyGridPopup;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUIWrapContentListView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.zxing.activity.CaptureActivity;

import java.util.*;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author SuccessZhang
 * @date 2019.4.22
 */
public class CommodityShelvesActivity extends AppCompatActivity implements View.OnClickListener {

    private QMUIRadiusImageView scan;
    private QMUIRoundButton sure;

    private QMUIWrapContentListView listView;

    private MyGridPopup myGridPopup;

    private CommodityShelvesListAdapter adapter;

    /**
     * 假数据
     */
    private List<String> names = new ArrayList<>(Arrays.asList("名称1", "名称2", "名称3", "名称4", "名称5"));
    private List<String> brands = new ArrayList<>(Arrays.asList("品牌1", "品牌2", "品牌3", "品牌4", "品牌5"));
    private List<String> msrps = new ArrayList<>(Arrays.asList("10", "20", "30", "40", "50"));

    private List<Store> stores;
    private int index = -1;
    private List<Map<String, String>> entities = new ArrayList<>();
    private List<String> storeNames = new ArrayList<>();

    /**
     * 饿汉式单例
     */
    private QMUIDialog dialog;

    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_shelves);
        action = getIntent().getStringExtra("action");
        User userInfo = JSON.parseObject(getIntent().getStringExtra("user")).toJavaObject(User.class);
        findView();
        addListener();
        //扫码上货,entity的selled字段置为0
        OkGo.<String>get("http://" + IP_AND_PORT + "/rest/v1/store/search?userId=" + userInfo.getId())
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.okgo.model.Response<String> response) {
                        String result = response.body();
                        if (result == null || "".equals(result)) {
                            Toast.makeText(CommodityShelvesActivity.this, "您没有已注册的实体店", Toast.LENGTH_SHORT).show();
                            CommodityShelvesActivity.this.finish();
                        }
                        stores = JSONObject.parseArray(result, Store.class);
                        for (Store store : Objects.requireNonNull(stores)) {
                            storeNames.add(store.getSname());
                        }
                        chooseStore();
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
        for (int i = 0; i < names.size(); i++) {
            addEntity(i);
        }
        adapter = new CommodityShelvesListAdapter(CommodityShelvesActivity.this, entities,
                R.layout.activity_commodity_shelves_list_item,
                new int[]{R.id.name, R.id.brand, R.id.msrp, R.id.delete}
        );
        listView.setAdapter(adapter);
        sure.setText(action);
    }

    private void addEntity(int i) {
        Map<String, String> map = new HashMap<>(3);
        map.put("name", names.get(i));
        map.put("brand", brands.get(i));
        map.put("msrp", msrps.get(i));
        entities.add(map);
    }

    private void chooseStore() {
        if (storeNames.isEmpty()) {
            Toast.makeText(CommodityShelvesActivity.this, "请先登记至少一个实体店", Toast.LENGTH_LONG).show();
            finish();
        }
        if (dialog == null) {
            dialog = new QMUIDialog.MenuDialogBuilder(CommodityShelvesActivity.this)
                    .setTitle("选择您的商店")
                    .addItems(storeNames.toArray(new String[]{}), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            index = which;
                            dialog.dismiss();
                        }
                    })
                    .create(R.style.QMUI_Dialog);
        }
        dialog.show();
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
            JSONObject json = new JSONObject();
            if ("确认\n上架".equals(action)) {
                if (index == -1) {
                    Toast.makeText(CommodityShelvesActivity.this, "请指定上架到哪个商店", Toast.LENGTH_SHORT).show();
                    chooseStore();
                } else {
                    json.put("storeId", stores.get(index).getId());
                    //todo 接口未完成,上货
                    json.put("goods", "");
                    OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/xxx")
                            .tag(this)
                            .upJson(json.toJSONString())
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    JSONObject body = JSON.parseObject(response.body());
                                    if (body != null) {

                                    }
                                }

                                @Override
                                public void onError(Response<String> response) {
                                    JSONObject jsonObject = JSON.parseObject(response.body());
                                    Toast.makeText(CommodityShelvesActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else if ("确认\n出售".equals(action)) {
                if (index == -1) {
                    Toast.makeText(CommodityShelvesActivity.this, "请指定您在哪个商店出售的该商品", Toast.LENGTH_SHORT).show();
                    chooseStore();
                } else {
                    QRCodeAdapter qrCodeAdapter = new QRCodeAdapter(
                            CommodityShelvesActivity.this,
                            adapter.getCountPrice(),
                            R.layout.activity_commodity_shelves_qr_code,
                            new int[]{R.id.qr_code},
                            stores.get(index).getId());
                    myGridPopup = new MyGridPopup(CommodityShelvesActivity.this, QMUIPopup.DIRECTION_BOTTOM, qrCodeAdapter, 1);
                    myGridPopup.create(QMUIDisplayHelper.dp2px(CommodityShelvesActivity.this, 300),
                            QMUIDisplayHelper.dp2px(CommodityShelvesActivity.this, 300), new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    myGridPopup.dismiss();
                                }
                            });
                    myGridPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                    myGridPopup.setPreferredDirection(QMUIPopup.DIRECTION_BOTTOM);
                    myGridPopup.show(listView);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Config.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = null;
            if (bundle != null) {
                scanResult = bundle.getString(Config.INTENT_EXTRA_KEY_QR_SCAN);
            }
            //将扫描出的信息添加到列表
            names.add("名称" + (names.size() + 1));
            brands.add("品牌" + (brands.size() + 1));
            msrps.add(scanResult);
            addEntity(names.size() - 1);
            adapter = new CommodityShelvesListAdapter(CommodityShelvesActivity.this, entities,
                    R.layout.activity_commodity_shelves_list_item,
                    new int[]{R.id.name, R.id.brand, R.id.msrp, R.id.delete}
            );
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
