package com.letmefold.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.R;
import com.letmefold.utils.Util;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author success zhang
 * @date 2019.1.15
 */
public class TypeActivity extends AppCompatActivity implements View.OnClickListener {

    private QMUIRadiusImageView seller;
    private QMUIRadiusImageView buyer;
    private JSONObject user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);
        Util.immersion(this, Color.WHITE, true);
        String json = getIntent().getStringExtra("user");
        user = JSON.parseObject(json);
        findView();
        addListener();
    }

    private void addListener() {
        seller.setOnClickListener(this);
        buyer.setOnClickListener(this);
    }

    private void findView() {
        seller = (QMUIRadiusImageView) findViewById(R.id.seller);
        buyer = (QMUIRadiusImageView) findViewById(R.id.buyer);
    }

    @Override
    public void onClick(View v) {
        if (seller == v) {
            user.put("type", "seller");
        } else if (buyer == v) {
            user.put("type", "buyer");
        }
        OkGo.<String>patch("http://" + IP_AND_PORT + "/rest/v1/user?id=" + user.getString("id") + "&type=" + user.getString("type"))
                .tag(this)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Looper.prepare();
                        JSONObject user = JSON.parseObject(response.body());
                        if (user != null) {
                            Toast.makeText(TypeActivity.this, "身份设置成功,您现在的身份是:" + user.getString("type"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(TypeActivity.this, MainActivity.class);
                            intent.putExtra("user", user.toJSONString());
                            startActivity(intent);
                            finish();
                        }
                        Looper.loop();
                    }

                    @Override
                    public void onError(com.okgo.model.Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        Toast.makeText(TypeActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 屏蔽物理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}