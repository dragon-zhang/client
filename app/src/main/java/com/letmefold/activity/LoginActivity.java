package com.letmefold.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.idl.face.platform.FaceConfig;
import com.baidu.idl.face.platform.FaceEnvironment;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.letmefold.Config;
import com.letmefold.DemoApplication;
import com.letmefold.R;
import com.letmefold.listener.BaseUiListener;
import com.letmefold.listener.SelfWbAuthListener;
import com.letmefold.utils.HttpUtil;
import com.letmefold.utils.Util;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.HashMap;
import java.util.Map;

import static com.letmefold.Config.IP_AND_PORT;
import static com.letmefold.DemoApplication.*;

/**
 * @author success zhang
 * @date 2018.12.9
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private TextView faceRegBtn;
    private TextView faceLoginBtn;
    private TextView regBtn;
    private CheckBox showPwd;
    private TextView forgetBtn;
    private EditText accEdit;
    private EditText pwdEdit;
    private Button loginBtn;
    private TextView smsLoginBtn;
    private QMUIRadiusImageView logo;

    private TextView qqText;
    private TextView sinaText;
    private QMUIRadiusImageView qq;
    private QMUIRadiusImageView sina;

    private SsoHandler mSsoHandler;

    private Map<String, Object> qqMap = new HashMap<>(25);
    private JSONObject qqUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Util.immersion(this, Color.WHITE, true);
        findView();
        effect();
        addListener();
        //整合SDK中的“活体检测”和“人脸图像采集”
        // 根据需求添加活体动作
        DemoApplication.livenessList.clear();
        DemoApplication.livenessList.add(LivenessTypeEnum.Eye);
        DemoApplication.livenessList.add(LivenessTypeEnum.Mouth);
        //DemoApplication.livenessList.add(LivenessTypeEnum.HeadUp)
        //DemoApplication.livenessList.add(LivenessTypeEnum.HeadDown)
        //DemoApplication.livenessList.add(LivenessTypeEnum.HeadLeft)
        //DemoApplication.livenessList.add(LivenessTypeEnum.HeadRight)
        DemoApplication.livenessList.add(LivenessTypeEnum.HeadLeftOrRight);

        initLib();
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 99);

        //重置本地用户信息
        /*PreferencesUtil.initPrefs(getApplicationContext());
        PreferencesUtil.remove("username");*/
    }

    private IUiListener loginListener = new BaseUiListener(LoginActivity.this) {
        @Override
        protected void doComplete(org.json.JSONObject values) {
            try {
                String token = values.getString(Constants.PARAM_ACCESS_TOKEN);
                String expires = values.getString(Constants.PARAM_EXPIRES_IN);
                String openId = values.getString(Constants.PARAM_OPEN_ID);
                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                        && !TextUtils.isEmpty(openId)) {
                    mTencent.setAccessToken(token, expires);
                    mTencent.setOpenId(openId);
                    qqMap.put(Constants.PARAM_ACCESS_TOKEN, token);
                    qqMap.put(Constants.PARAM_EXPIRES_IN, expires);
                    qqMap.put(Constants.PARAM_OPEN_ID, openId);
                }
            } catch (Exception ignored) {
            }
            updateUserInfo();
        }
    };

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/user/register")
                        .tag(this)
                        .upJson(sinaUserInfo.toJSONString())
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                JSONObject user = JSON.parseObject(response.body());
                                if (user != null) {
                                    gotoMain(user);
                                    Toast.makeText(LoginActivity.this, "sina登录成功", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(com.okgo.model.Response<String> response) {
                                JSONObject jsonObject = JSON.parseObject(response.body());
                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            return false;
        }
    });

    /**
     * 初始化SDK
     */
    private void initLib() {
        // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
        // 应用上下文
        // 申请License取得的APPID
        // assets目录下License文件名
        FaceSDKManager.getInstance().initialize(LoginActivity.this, Config.LICENSE_ID, Config.LICENSE_FILE_NAME, com.baidu.aip.FaceSDKManager.getInstance().getFaceTracker(this));
        setFaceConfig();
    }

    private void setFaceConfig() {
        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
        config.setLivenessTypeList(DemoApplication.livenessList);
        config.setLivenessRandom(DemoApplication.isLivenessRandom);
        config.setBlurnessValue(FaceEnvironment.VALUE_BLURNESS);
        config.setBrightnessValue(FaceEnvironment.VALUE_BRIGHTNESS);
        config.setCropFaceValue(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        config.setHeadPitchValue(FaceEnvironment.VALUE_HEAD_PITCH);
        config.setHeadRollValue(FaceEnvironment.VALUE_HEAD_ROLL);
        config.setHeadYawValue(FaceEnvironment.VALUE_HEAD_YAW);
        config.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        config.setNotFaceValue(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        config.setOcclusionValue(FaceEnvironment.VALUE_OCCLUSION);
        config.setCheckFaceQuality(true);
        config.setFaceDecodeNumberOfThreads(2);
        FaceSDKManager.getInstance().setFaceConfig(config);
    }

    private void effect() {
        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.setDuration(1000);
        //获取屏幕高度
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(metrics);
        }
        int screenHeight = metrics.heightPixels;
        //通过测量，获取logo的高度
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        logo.measure(w, h);
        int logoHeight = logo.getMeasuredHeight();
        //初始化ivLogo的移动和缩放动画
        float transY = (screenHeight - logoHeight) * 0.8f;
        //logo在X轴和Y轴上都缩放0.8倍
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 0.8f);
        //logo向上移动 transY 的距离
        ObjectAnimator tranLogo = ObjectAnimator.ofFloat(logo, "translationY", 0, -transY);
        //qq、微信、新浪微博LOGO均下移
        qq.measure(w, h);
        int qqHeight = qq.getMeasuredHeight();
        float down = (screenHeight / 2f - qqHeight) * 0.75f;
        ObjectAnimator qqLogo = ObjectAnimator.ofFloat(qq, "translationY", 0, down);
        ObjectAnimator sinaLogo = ObjectAnimator.ofFloat(sina, "translationY", 0, down);

        logoAnim.play(scaleXLogo).with(scaleYLogo)
                .with(qqLogo).with(sinaLogo)
                .with(ObjectAnimator.ofFloat(qqText, "translationY", 0, down))
                .with(ObjectAnimator.ofFloat(sinaText, "translationY", 0, down))
                .with(ObjectAnimator.ofFloat(loginBtn, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(pwdEdit, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(forgetBtn, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(showPwd, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(accEdit, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(faceLoginBtn, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(smsLoginBtn, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(regBtn, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(faceRegBtn, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(qqText, "alpha", 0, 0))
                .with(ObjectAnimator.ofFloat(sinaText, "alpha", 0, 0));
        logoAnim.play(tranLogo)
                //将其余控件从不可见到可见
                .with(ObjectAnimator.ofFloat(loginBtn, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(pwdEdit, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(forgetBtn, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(showPwd, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(accEdit, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(faceLoginBtn, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(smsLoginBtn, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(regBtn, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(faceRegBtn, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(qqText, "alpha", 0, 1))
                .with(ObjectAnimator.ofFloat(sinaText, "alpha", 0, 1))
                .after(scaleXLogo);
        logoAnim.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findView() {
        faceRegBtn = (TextView) findViewById(R.id.face_reg_btn);
        faceLoginBtn = (TextView) findViewById(R.id.face_login_btn);
        regBtn = (TextView) findViewById(R.id.reg_btn);
        showPwd = (CheckBox) findViewById(R.id.show_password);
        accEdit = (EditText) findViewById(R.id.acc_edit);
        pwdEdit = (EditText) findViewById(R.id.pwd_edit);
        forgetBtn = (TextView) findViewById(R.id.forget);
        loginBtn = (Button) findViewById(R.id.login_in);
        smsLoginBtn = (TextView) findViewById(R.id.sms_login_btn);
        logo = (QMUIRadiusImageView) findViewById(R.id.logo);
        qqText = (TextView) findViewById(R.id.qq_text);
        sinaText = (TextView) findViewById(R.id.sina_text);
        qq = (QMUIRadiusImageView) findViewById(R.id.qq);
        sina = (QMUIRadiusImageView) findViewById(R.id.sina);
    }

    static void showPassword(boolean isChecked, EditText editText) {
        if (isChecked) {
            //密码可见
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            //密码不可见
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        //把光标位置挪到文字的最后面
        editText.setSelection(editText.getText().length());
    }

    private void addListener() {
        faceRegBtn.setOnClickListener(this);
        faceLoginBtn.setOnClickListener(this);
        regBtn.setOnClickListener(this);
        forgetBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        smsLoginBtn.setOnClickListener(this);
        showPwd.setOnCheckedChangeListener(this);
        qq.setOnClickListener(this);
        sina.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }
        if (faceRegBtn == v) {
            startActivity(new Intent(LoginActivity.this, FaceRegActivity.class));
        } else if (faceLoginBtn == v) {
            //实时人脸检测
            startActivity(new Intent(LoginActivity.this, DetectLoginActivity.class));
        } else if (regBtn == v) {
            Intent intent = new Intent(LoginActivity.this, RegActivity.class);
            intent.putExtra("todo", "register");
            startActivity(intent);
        } else if (forgetBtn == v) {
            Intent intent = new Intent(LoginActivity.this, RegActivity.class);
            intent.putExtra("todo", "forget");
            startActivity(intent);
        } else if (loginBtn == v) {
            commonLogin();
        } else if (smsLoginBtn == v) {
            Intent intent = new Intent(LoginActivity.this, SmsLoginActivity.class);
            startActivity(intent);
            finish();
        } else if (qq == v) {
            qqLogin();
        } else if (sina == v) {
            sinaLogin();
        }
    }

    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new BaseUiListener(LoginActivity.this) {

                @Override
                public void onError(UiError e) {
                }

                @Override
                public void onComplete(final Object response) {
                    qqMap.putAll(JSONObject.parseObject(response.toString()));
                    qqUserInfo = new JSONObject(qqMap);
                    qqUserInfo.put("register_type", "qq");
                    //用户信息改变，重新登陆
                    OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/user/register")
                            .tag(this)
                            .upJson(qqUserInfo.toJSONString())
                            .execute(new StringCallback() {
                                @Override
                                public void onSuccess(Response<String> response) {
                                    JSONObject user = JSON.parseObject(response.body());
                                    if (user != null) {
                                        gotoMain(user);
                                        Toast.makeText(LoginActivity.this, "qq登录成功", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(Response<String> response) {
                                    JSONObject jsonObject = JSON.parseObject(response.body());
                                    Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onCancel() {
                }
            };
            UserInfo mInfo = new UserInfo(this, mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //QQ的回调
        if (requestCode == Constants.REQUEST_LOGIN || requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
        //SSO授权回调，发起SSO登陆的Activity必须重写onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        showPassword(isChecked, pwdEdit);
    }

    private void gotoMain(JSONObject user) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user", user.toJSONString());
        startActivity(intent);
        LoginActivity.this.finish();
    }

    private void commonLogin() {
        Map<String, Object> map = new HashMap<>(2);
        String account = accEdit.getText().toString();
        String password = pwdEdit.getText().toString();
        map.put("account", account);
        map.put("password", password);
        JSONObject json = new JSONObject(map);
        OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/user/login")
                .tag(this)
                .upJson(json.toJSONString())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.okgo.model.Response<String> response) {
                        Looper.prepare();
                        JSONObject user = JSON.parseObject(response.body());
                        if (user != null) {
                            gotoMain(user);
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        }
                        Looper.loop();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void qqLogin() {
        //是否已经安装了QQ
        if (mTencent.isQQInstalled(LoginActivity.this)) {
            org.json.JSONObject jsonObject;
            if (!mTencent.checkSessionValid(mAppid)) {
                Toast.makeText(LoginActivity.this, "正在拉起QQ授权", Toast.LENGTH_SHORT).show();
                if (!mTencent.isSessionValid()) {
                    mTencent.login(this, "all", loginListener, false);
                    isServerSideLogin = false;
                } else {
                    if (isServerSideLogin) {
                        // Server-Side 模式的登陆, 先退出，再进行SSO登陆
                        mTencent.logout(this);
                        mTencent.login(this, "all", loginListener);
                        isServerSideLogin = false;
                        return;
                    }
                    mTencent.logout(this);
                    updateUserInfo();
                }
            } else {
                jsonObject = mTencent.loadSession(mAppid);
                mTencent.initSessionCache(jsonObject);
                qqMap.putAll(JSONObject.parseObject(jsonObject.toString()));
            }
            updateUserInfo();
        } else {
            Toast.makeText(LoginActivity.this, "未安装QQ", Toast.LENGTH_LONG).show();
        }
    }

    private void sinaLogin() {
        if (!mAccessToken.isSessionValid()) {
            //token过期，重新登录
            /*创建微博实例，注意：SsoHandler 仅当 SDK 支持 SSO 时有效*/
            mSsoHandler = new SsoHandler(LoginActivity.this);
            //SSO授权,如果手机安装了微博客户端则使用客户端授权,没有则进行网页授权
            mSsoHandler.authorize(new SelfWbAuthListener(LoginActivity.this, mHandler));
        } else {
            // 从 SharedPreferences 中读取上次已保存好 AccessToken 等信息
            mAccessToken = AccessTokenKeeper.readAccessToken(this);
            HttpUtil.initSinaUserInfoAndLogin(mHandler);
        }
    }
}