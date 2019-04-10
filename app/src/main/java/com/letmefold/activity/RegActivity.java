package com.letmefold.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import cn.smssdk.SMSSDK;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.R;
import com.letmefold.sms.SmsObserver;
import com.letmefold.utils.Util;
import com.letmefold.watcher.MailWatcher;
import com.letmefold.watcher.PasswordWatcher;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import org.jsoup.helper.StringUtil;

import java.util.HashMap;
import java.util.Map;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author success zhang
 * @date 2018.12.9
 */
public class RegActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

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

    private final String CODE_REGISTER = "register";

    private String mPhone;
    private SmsObserver smsObserver;

    private EditText etPhone;
    private EditText etCode;
    private Button btnGetCode;
    private Button verify;
    private RelativeLayout pwd1;
    private RelativeLayout pwd2;
    private Button register;
    private EditText pwdEdit1;
    private CheckBox showPwd1;
    private EditText pwdEdit2;
    private CheckBox showPwd2;
    private EditText mailEdit;
    private TextView pwdWarn;
    private TextView mailWarn;
    private ImageButton back;
    private String todo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        Util.immersion(this, getResources().getColor(R.color.qmui_config_color_blue), false);
        findView();
        todo = getIntent().getStringExtra("todo");
        String codeForget = "forget";
        if (codeForget.equals(todo)) {
            register.setText("重设密码");
        }
        addListener();
        smsObserver = new SmsObserver(new Handler(), RegActivity.this, etCode);
        Util.initSMSSDK(mHandler);
    }

    private void findView() {
        etPhone = (EditText) findViewById(R.id.et_phone);
        etCode = (EditText) findViewById(R.id.et_code);
        btnGetCode = (Button) findViewById(R.id.btn_getCode);
        verify = (Button) findViewById(R.id.btn_verify);
        pwd1 = (RelativeLayout) findViewById(R.id.pwd1);
        pwd2 = (RelativeLayout) findViewById(R.id.pwd2);
        register = (Button) findViewById(R.id.register);
        pwdEdit1 = (EditText) findViewById(R.id.pwd_edit1);
        pwdEdit2 = (EditText) findViewById(R.id.pwd_edit2);
        showPwd1 = (CheckBox) findViewById(R.id.show_password1);
        showPwd2 = (CheckBox) findViewById(R.id.show_password2);
        mailEdit = (EditText) findViewById(R.id.mail_edit);
        pwdWarn = (TextView) findViewById(R.id.pwd_warn);
        mailWarn = (TextView) findViewById(R.id.mail_warn);
        back = (ImageButton) findViewById(R.id.btn_back);
    }

    private void addListener() {
        btnGetCode.setOnClickListener(this);
        verify.setOnClickListener(this);
        register.setOnClickListener(this);
        back.setOnClickListener(this);
        pwdEdit2.addTextChangedListener(new PasswordWatcher(pwdEdit1, pwdWarn, register));
        mailEdit.addTextChangedListener(new MailWatcher(mailWarn));
        showPwd1.setOnCheckedChangeListener(this);
        showPwd2.setOnCheckedChangeListener(this);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SUCCESS:
                    //获取成功处理
                    Toast.makeText(RegActivity.this, "获取验证码成功....", Toast.LENGTH_SHORT).show();
                    getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
                    break;
                case SUBMIT_SUCCESS:
                    //验证成功处理
                    Toast.makeText(RegActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                    mTimer.cancel();
                    if (CODE_REGISTER.equals(todo)) {
                        mailEdit.setVisibility(View.VISIBLE);
                    }
                    pwd1.setVisibility(View.VISIBLE);
                    pwd2.setVisibility(View.VISIBLE);
                    break;
                case CHECK_FAIL:
                    //服务器返回错误处理
                    Throwable data = (Throwable) msg.obj;
                    Toast.makeText(RegActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
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
            btnGetCode.setText(String.format(getResources().getString(R.string.delay_time), millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            //倒计时结束时，回调该方法
            btnGetCode.setText("重新获取");
            btnGetCode.setEnabled(true);
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
        if (btnGetCode == v) {
            mPhone = etPhone.getText().toString();
            Util.getCode(mPhone, btnGetCode, mTimer, this);
        } else if (verify == v) {
            //验证验证码
            String mCode = etCode.getText().toString();
            //判断手机号和验证码都不为空
            if (!TextUtils.isEmpty(mCode) && !TextUtils.isEmpty(mPhone)) {
                //提交验证信息，结果都在EventHandler监听返回
                SMSSDK.submitVerificationCode("86", mPhone, mCode);
            }
        } else if (register == v) {
            Map<String, Object> map = new HashMap<>(4);
            map.put("register_type", "common");
            map.put("mobile", mPhone);
            map.put("password", pwdEdit1.getText().toString());
            if (CODE_REGISTER.equals(todo)) {
                String mail = mailEdit.getText().toString();
                if (!StringUtil.isBlank(mail)) {
                    map.put("email", mail);
                }
            }
            JSONObject json = new JSONObject(map);
            OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/user/register")
                    .tag(this)
                    .upJson(json.toJSONString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(com.okgo.model.Response<String> response) {
                            Log.i("FaceRegActivity", "orientation->" + response.body());
                            Toast.makeText(RegActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
        } else if (back == v) {
            RegActivity.this.finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == showPwd1) {
            LoginActivity.showPassword(isChecked, pwdEdit1);
        } else if (buttonView == showPwd2) {
            LoginActivity.showPassword(isChecked, pwdEdit2);
        }
    }
}