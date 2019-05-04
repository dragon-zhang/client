package com.letmefold.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.Config;
import com.letmefold.R;
import com.letmefold.activity.user.CardIssueActivity;
import com.letmefold.activity.user.CommodityShelvesActivity;
import com.letmefold.activity.user.StoreRegisterActivity;
import com.letmefold.adapter.MainListAdapter;
import com.letmefold.pojo.Card;
import com.letmefold.pojo.CardDetail;
import com.letmefold.pojo.LeasedCardVO;
import com.letmefold.pojo.Store;
import com.letmefold.utils.ImageSaveUtil;
import com.letmefold.utils.Util;
import com.letmefold.view.MyGridPopup;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.zxing.activity.CaptureActivity;

import java.util.*;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author success zhang
 * @date 2018.12.11
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private long exitTime;
    private QMUIRadiusImageView scan;
    private GridView option;
    private QMUIRadiusImageView user;
    private ListView listView;
    private LinearLayout titleLayout;
    private MyGridPopup myGridPopup;

    private JSONObject userInfo;
    private List<Store> stores;
    private List<Card> cards;
    private List<CardDetail> cardDetails;

    private Map<String, String> params = new HashMap<>(5);
    private Set<String> locations = new HashSet<>();
    private Set<String> snames = new HashSet<>();
    private Set<String> scopes = new HashSet<>();
    private Set<String> versions = new HashSet<>();
    private Set<String> grades = new HashSet<>();

    private List<LeasedCardVO> leasedCards;
    private QMUIBottomSheet.BottomListSheetBuilder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.immersion(this, Color.WHITE, true);
        String json = getIntent().getStringExtra("user");
        userInfo = JSON.parseObject(json);
        if (userInfo.getString("type") == null) {
            Intent intent = new Intent(MainActivity.this, TypeActivity.class);
            intent.putExtra("user", userInfo.toJSONString());
            startActivity(intent);
        }
        findView();
        addListener();
        addAdapter();
        initAvatar();
        initData();
    }

    private void initAvatar() {
        String faceId = userInfo.getString("faceId");
        if (faceId != null && !"".equals(faceId)) {
            Bitmap avatar = ImageSaveUtil.loadCameraBitmap(this, "head_tmp.jpg");
            if (avatar != null) {
                user.setImageBitmap(avatar);
            }
        }
    }

    private void initData() {
        //请求所有商店的数据
        getStores(null, null, null);
        //请求所有卡的数据
        getCardDetails(null, null, null, null, null);
    }

    private void addAdapter() {
        String[] items = {"地点", "店名", "经营范围", "发行版本", "卡等级"};
        List<Map<String, Object>> data = new ArrayList<>();
        for (String item : items) {
            Map<String, Object> map = new HashMap<>(1);
            map.put("item", item);
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, data,
                R.layout.activity_main_grid_item,
                new String[]{"item"},
                new int[]{R.id.item});

        // item宽度
        int itemWidth = dip2px(this, 75);
        // item之间的间隔
        int itemPaddingH = dip2px(this, 5);
        int size = items.length;
        // 计算GridView宽度
        int gridviewWidth = size * (itemWidth + itemPaddingH);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        option.setLayoutParams(params);
        option.setColumnWidth(itemWidth);
        option.setHorizontalSpacing(itemPaddingH);
        option.setNumColumns(size);
        option.setAdapter(adapter);
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void addListener() {
        scan.setOnClickListener(this);
        user.setOnClickListener(this);
        option.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initListPopupIfNeed(position, view);
            }
        });
    }

    private void initListPopupIfNeed(int position, View view) {
        switch (position) {
            case 0:
                //地点
                for (Store store : Objects.requireNonNull(stores)) {
                    locations.add(store.getLocation());
                }
                createGridPopup(locations, position);
                break;
            case 1:
                //店名
                for (Store store : Objects.requireNonNull(stores)) {
                    snames.add(store.getSname());
                }
                createGridPopup(snames, position);
                break;
            case 2:
                //经营范围
                for (Store store : Objects.requireNonNull(stores)) {
                    scopes.add(store.getScope());
                }
                createGridPopup(scopes, position);
                break;
            case 3:
                //发行版本
                for (Card card : Objects.requireNonNull(cards)) {
                    versions.add(card.getIssueVersion());
                }
                createGridPopup(versions, position);
                break;
            case 4:
                //卡等级
                for (Card card : Objects.requireNonNull(cards)) {
                    grades.add(card.getGrade());
                }
                createGridPopup(grades, position);
                break;
            default:
                break;
        }
        myGridPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
        myGridPopup.setPreferredDirection(QMUIPopup.DIRECTION_BOTTOM);
        myGridPopup.setPositionOffsetYWhenBottom(-60);
        myGridPopup.show(view);
    }

    private void createGridPopup(Set<String> setData, final int position) {
        final List<String> data = new ArrayList<>(setData);
        ArrayAdapter adapter = new ArrayAdapter<>(MainActivity.this, R.layout.simple_text_item, data);
        myGridPopup = new MyGridPopup(MainActivity.this, QMUIPopup.DIRECTION_NONE, adapter, 2);
        myGridPopup.create(QMUIDisplayHelper.dp2px(MainActivity.this, 250), QMUIDisplayHelper.dp2px(MainActivity.this, 200), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String param = data.get(i);
                Toast.makeText(MainActivity.this, param, Toast.LENGTH_SHORT).show();
                switch (position) {
                    case 0:
                        params.put("location", param);
                        break;
                    case 1:
                        params.put("sname", param);
                        break;
                    case 2:
                        params.put("scope", param);
                        break;
                    case 3:
                        params.put("version", param);
                        break;
                    case 4:
                        params.put("grade", param);
                        break;
                    default:
                        break;
                }
                if (position < 3) {
                    getStores(params.get("location"), params.get("sname"), params.get("scope"));
                } else {
                    getCards(params.get("version"), params.get("grade"));
                }
                getCardDetails(params.get("location"), params.get("sname"), params.get("scope"), params.get("version"), params.get("grade"));
                myGridPopup.dismiss();
            }
        });
    }

    private void getCardDetails(String location, String sname, String scope, String version, String grade) {
        if (location == null) {
            location = "";
        }
        if (sname == null) {
            sname = "";
        }
        if (scope == null) {
            scope = "";
        }
        if (version == null) {
            version = "";
        }
        if (grade == null) {
            grade = "";
        }
        OkGo.<String>get("http://" + IP_AND_PORT + "/rest/v1/card/fuzzy?" +
                "&location=" + location +
                "&sname=" + sname +
                "&scope=" + scope +
                "&version=" + version +
                "&grade=" + grade)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.okgo.model.Response<String> response) {
                        String result = response.body();
                        if (result == null || "".equals(result)) {
                            Toast.makeText(MainActivity.this, "没有任何发行卡信息", Toast.LENGTH_SHORT).show();
                            MainActivity.this.finish();
                        }
                        JSONObject json = JSON.parseObject(result);
                        if ("OK".equals(json.getString("msg"))) {
                            cardDetails = JSONObject.parseArray(json.getString("data"), CardDetail.class);

                            MainListAdapter adapter = new MainListAdapter(MainActivity.this, cardDetails,
                                    R.layout.activity_main_list_item,
                                    new int[]{R.id.sname, R.id.location, R.id.scope, R.id.version, R.id.grade, R.id.time, R.id.lease},
                                    userInfo.getString("id"));
                            listView.setAdapter(adapter);
                            adapter.measureMaxWidth(listView);
                            adapter.addTitles(titleLayout, new String[]{"店铺名称", "店铺位置", "经营范围", "发行版本", "卡等级", "发行时间", "租卡"});

                            if (cards == null) {
                                cards = new ArrayList<>();
                                for (CardDetail detail : Objects.requireNonNull(cardDetails)) {
                                    cards.add(detail.toCard());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void findView() {
        scan = (QMUIRadiusImageView) findViewById(R.id.scan);
        option = (GridView) findViewById(R.id.option);
        user = (QMUIRadiusImageView) findViewById(R.id.user);
        listView = (ListView) findViewById(R.id.cards);
        titleLayout = (LinearLayout) findViewById(R.id.titleLayout);
    }

    @Override
    public void onClick(View v) {
        if (scan == v) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 申请权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Config.REQ_PERM_CAMERA);
            } else {
                // 二维码扫码
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, Config.REQ_QR_CODE);
            }
        } else if (user == v) {
            String[] items = new String[]{"重选身份"};
            if ("seller".equals(userInfo.getString("type"))) {
                List<String> list = new ArrayList<>(Arrays.asList(items));
                list.addAll(Arrays.asList("实体店登记", "商品出售", "会员卡发行", "商品上架"));
                items = list.toArray(new String[]{});
            }
            new QMUIDialog.MenuDialogBuilder(MainActivity.this)
                    .addItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = null;
                            switch (which) {
                                case 0:
                                    intent = new Intent(MainActivity.this, TypeActivity.class);
                                    break;
                                case 1:
                                    intent = new Intent(MainActivity.this, StoreRegisterActivity.class);
                                    break;
                                case 2:
                                    intent = new Intent(MainActivity.this, CommodityShelvesActivity.class);
                                    intent.putExtra("action", "确认\n出售");
                                    break;
                                case 3:
                                    intent = new Intent(MainActivity.this, CardIssueActivity.class);
                                    break;
                                case 4:
                                    intent = new Intent(MainActivity.this, CommodityShelvesActivity.class);
                                    intent.putExtra("action", "确认\n上架");
                                    break;
                                default:
                                    break;
                            }
                            if (intent != null) {
                                intent.putExtra("user", userInfo.toJSONString());
                                startActivityForResult(intent, Config.RE_INIT_DATA);
                            }
                            dialog.dismiss();
                        }
                    })
                    .create(R.style.QMUI_Dialog).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.REQ_QR_CODE && resultCode == RESULT_OK) {
            //扫描结果回调
            Bundle bundle = data.getExtras();
            String scanResult = null;
            if (bundle != null) {
                scanResult = bundle.getString(Config.INTENT_EXTRA_KEY_QR_SCAN);
            }
            if (scanResult != null) {
                //将扫描出的信息显示出来
                final String pay = scanResult.substring(scanResult.indexOf("-") + 1);
                Toast.makeText(MainActivity.this, "原价:" + pay, Toast.LENGTH_LONG).show();
                builder = new QMUIBottomSheet.BottomListSheetBuilder(MainActivity.this)
                        .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                dialog.dismiss();
                                String msg;
                                if (position < leasedCards.size()) {
                                    msg = "使用 " + leasedCards.get(position).getCard().getGrade() + " 卡";
                                } else {
                                    msg = "我是土豪,不用卡";
                                }
                                msg = msg + ",支付" + pay + "元";
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                //查询用户已租的卡
                OkGo.<String>get("http://" + IP_AND_PORT + "/rest/v1/leasedcard/query?tenantId=" + userInfo.getString("id"))
                        .tag(this)
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(com.okgo.model.Response<String> response) {
                                JSONObject result = JSONObject.parseObject(response.body());
                                if ("OK".equals(result.getString("msg"))) {
                                    leasedCards = JSONObject.parseArray(result.getString("data"), LeasedCardVO.class);
                                    for (LeasedCardVO leasedCard : leasedCards) {
                                        builder.addItem(leasedCard.getCard().getGrade());
                                    }
                                }
                                builder.addItem("我是土豪,不用卡");
                                builder.build().show();
                            }

                            @Override
                            public void onError(Response<String> response) {
                                JSONObject jsonObject = JSON.parseObject(response.body());
                                Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else if (requestCode == Config.RE_INIT_DATA && resultCode == RESULT_OK) {
            //需要重新请求数据
            initData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Config.REQ_PERM_CAMERA) {
            // 摄像头权限申请
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 获得授权
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 申请权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Config.REQ_PERM_CAMERA);
                }
            } else {
                // 被禁止授权
                Toast.makeText(MainActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCards(String version, String grade) {
        if (version == null) {
            version = "";
        }
        if (grade == null) {
            grade = "";
        }
        //请求符合条件的卡的数据
        OkGo.<String>get("http://" + IP_AND_PORT + "/rest/v1/card/all?version=" + version + "&grade=" + grade)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.okgo.model.Response<String> response) {
                        String result = response.body();
                        if (result == null || "".equals(result)) {
                            Toast.makeText(MainActivity.this, "没有任何发行卡信息", Toast.LENGTH_SHORT).show();
                            MainActivity.this.finish();
                        }
                        cards = JSONObject.parseArray(result, Card.class);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getStores(String location, String sname, String scope) {
        if (location == null) {
            location = "";
        }
        if (sname == null) {
            sname = "";
        }
        if (scope == null) {
            scope = "";
        }
        //请求符合条件的卡的数据
        OkGo.<String>get("http://" + IP_AND_PORT + "/rest/v1/store/all?location=" + location + "&sname=" + sname + "&scope=" + scope)
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.okgo.model.Response<String> response) {
                        String result = response.body();
                        if (result == null || "".equals(result)) {
                            Toast.makeText(MainActivity.this, "没有任何商店信息", Toast.LENGTH_SHORT).show();
                            MainActivity.this.finish();
                        }
                        stores = JSONObject.parseArray(result, Store.class);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 重写返回键，实现双击退出效果
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > 3000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                MainActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}