package com.letmefold.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import cn.smssdk.SMSSDK;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.R;
import com.letmefold.sms.SmsObserver;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.letmefold.Config.IP_AND_PORT;
import static com.letmefold.utils.Util.*;

/**
 * @author success zhang
 * @date 2018.12.9
 */
public class SmsLoginActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 获取验证码成功
     */
    private static final int GET_SUCCESS = 1;
    /**
     * 验证成功
     */
    private static final int SUBMIT_SUCCESS = 2;
    /**
     * 检查失败
     */
    private static final int CHECK_FAIL = 3;

    private String mPhone;
    private SmsObserver smsObserver;

    private EditText etPhoneLogin;
    private EditText etCodeLogin;
    private Button getCode;
    private Button verify;
    private Button login;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_login);
        immersion(this, getResources().getColor(R.color.qmui_config_color_blue), false);
        findView();
        addListener();
        smsObserver = new SmsObserver(new Handler(), SmsLoginActivity.this, etCodeLogin);
        initSMSSDK(mHandler);
    }

    private void findView() {
        etPhoneLogin = (EditText) findViewById(R.id.et_phone_login);
        etCodeLogin = (EditText) findViewById(R.id.et_code_login);
        getCode = (Button) findViewById(R.id.btn_getCode_login);
        verify = (Button) findViewById(R.id.btn_verify_login);
        login = (Button) findViewById(R.id.login);
        back = (ImageButton) findViewById(R.id.btn_back);
    }

    private void addListener() {
        getCode.setOnClickListener(this);
        verify.setOnClickListener(this);
        login.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SUCCESS:
                    //获取成功处理
                    Toast.makeText(SmsLoginActivity.this, "获取验证码成功....", Toast.LENGTH_SHORT).show();
                    getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
                    break;
                case SUBMIT_SUCCESS:
                    //验证成功处理
                    Toast.makeText(SmsLoginActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                    mTimer.cancel();
                    login.setEnabled(true);
                    break;
                case CHECK_FAIL:
                    //服务器返回错误处理
                    Throwable data = (Throwable) msg.obj;
                    Toast.makeText(SmsLoginActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 倒计时60s，使用CountDownTimer类，只需实现onTick()和onFinish()方法
     */
    private CountDownTimer mTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            //时间间隔固定回调该方法
            getCode.setText(String.format(getResources().getString(R.string.delay_time), millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            //倒计时结束时，回调该方法
            getCode.setText("重新获取");
            getCode.setEnabled(true);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销所有EventHandler监听，避免内存泄露
        SMSSDK.unregisterAllEventHandler();
        getContentResolver().unregisterContentObserver(smsObserver);
    }

    @Override
    public void onClick(View v) {
        if (getCode == v) {
            mPhone = etPhoneLogin.getText().toString();
            getCode(mPhone, getCode, mTimer, this);
        } else if (verify == v) {
            //验证验证码
            String mCode = etCodeLogin.getText().toString();
            //判断手机号和验证码都不为空
            if (!TextUtils.isEmpty(mCode) && !TextUtils.isEmpty(mPhone)) {
                //提交验证信息，结果都在EventHandler监听返回
                SMSSDK.submitVerificationCode("86", mPhone, mCode);
            }
        } else if (login == v) {
            OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/user/login/sms")
                    .tag(this)
                    .upRequestBody(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), mPhone))
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(com.okgo.model.Response<String> response) {
                            JSONObject user = JSON.parseObject(response.body());
                            if (user != null) {
                                Intent intent = new Intent(SmsLoginActivity.this, MainActivity.class);
                                intent.putExtra("user", user.toJSONString());
                                startActivity(intent);
                                SmsLoginActivity.this.finish();
                                Toast.makeText(SmsLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SmsLoginActivity.this, "请先进行注册", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            JSONObject jsonObject = JSON.parseObject(response.body());
                            Toast.makeText(SmsLoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (back == v) {
            Intent intent = new Intent(SmsLoginActivity.this, LoginActivity.class);
            startActivity(intent);
            SmsLoginActivity.this.finish();
        }
    }
}