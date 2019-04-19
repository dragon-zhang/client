package com.letmefold.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.WriterException;
import com.letmefold.Config;
import com.letmefold.R;
import com.letmefold.utils.BarCodeUtil;
import com.letmefold.utils.QRCodeUtil;
import com.letmefold.utils.Util;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.QMUIWrapContentListView;
import com.zxing.activity.CaptureActivity;

import static com.mob.MobSDK.getContext;

/**
 * @author success zhang
 * @date 2018.12.11
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private long exitTime;
    private QMUIRadiusImageView scan;
    private QMUIRadiusImageView qrCode;
    private TextView result;
    private QMUIRadiusImageView barCode;
    private QMUIWrapContentListView option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Util.immersion(this, Color.WHITE, true);

        String json = getIntent().getStringExtra("user");
        JSONObject user = JSON.parseObject(json);
        if (user.getString("type") == null) {
            Intent intent = new Intent(MainActivity.this, TypeActivity.class);
            intent.putExtra("user", user.toJSONString());
            startActivity(intent);
        }

        findView();
        addListener();
        addAdapter();
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.logo_square);
        try {
            qrCode.setImageBitmap(QRCodeUtil.createQRCodeBitmapWithImage("哈哈哈", 300, 300, "2",
                    "utf-8", "H",
                    bitmap, 60));
            barCode.setImageBitmap(BarCodeUtil.createBarCodeBitmap("012345678912", 320, 80, true));
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    private void addAdapter() {
        //假数据
        String[] data = {"aa", "bb", "cc", "dd", "aa", "bb", "cc", "dd", "aa", "bb", "cc", "dd", "aa", "bb", "cc", "dd"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        option.setAdapter(adapter);
    }

    private void addListener() {
        scan.setOnClickListener(this);
    }

    private void findView() {
        qrCode = (QMUIRadiusImageView) findViewById(R.id.qr_code);
        scan = (QMUIRadiusImageView) findViewById(R.id.scan);
        result = (TextView) findViewById(R.id.qr_result);
        barCode = (QMUIRadiusImageView) findViewById(R.id.bar_code);
        option = (QMUIWrapContentListView) findViewById(R.id.option);
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
            //将扫描出的信息显示出来
            result.setText(scanResult);
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