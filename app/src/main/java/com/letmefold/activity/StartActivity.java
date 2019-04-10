package com.letmefold.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import com.letmefold.R;
import com.letmefold.utils.Util;

/**
 * @author success zhang
 * @date 2018.12.19
 */
public class StartActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Util.immersion(this, Color.WHITE, true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //前往注册、登录主页
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                //取消界面跳转时的动画，使启动页的logo图片与注册、登录主页的logo图片完美衔接
                overridePendingTransition(0, 0);
            }
        }, 3000);
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

    @Override
    protected void onDestroy() {
        if (handler != null) {
            //If token is null, all callbacks and messages will be removed.
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}