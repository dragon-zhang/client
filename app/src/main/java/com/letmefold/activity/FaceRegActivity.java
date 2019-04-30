/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.FaceSDKManager;
import com.letmefold.Config;
import com.letmefold.R;
import com.letmefold.utils.ImageSaveUtil;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Progress;
import com.okgo.model.Response;
import com.okgo.request.base.Request;

import java.io.File;
import java.io.IOException;

import static com.letmefold.Config.IP_AND_PORT;
import static com.letmefold.utils.Base64RequestBody.readFile;

/**
 * @author baidu
 * 该类提供人脸注册功能，注册的人脸可以通个自动检测和选自相册两种方式获取。
 */
public class FaceRegActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_DETECT_FACE = 1000;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private EditText usernameEt;
    private ImageView avatarIv;
    private Button pickFromAlbumBtn;
    private Button submitBtn;
    private String facePath;
    private Bitmap mHeadBmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_reg);

        init();
        findView();
        addListener();

        String isLive = getIntent().getStringExtra("isLive");
        if ("live".equals(isLive)) {
            Intent it = new Intent(FaceRegActivity.this, FaceDetectActivity.class);
            startActivityForResult(it, REQUEST_CODE_DETECT_FACE);
        } else if (isLive == null) {
            startActivity(new Intent(FaceRegActivity.this, FaceLivenessExpActivity.class));
            finish();
        }
    }

    private void findView() {
        usernameEt = (EditText) findViewById(R.id.username_et);
        avatarIv = (ImageView) findViewById(R.id.avatar_iv);
        pickFromAlbumBtn = (Button) findViewById(R.id.pick_from_album_btn);
        submitBtn = (Button) findViewById(R.id.submit_btn);
    }

    private void addListener() {
        pickFromAlbumBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
    }

    private void init() {
        // 如果图片中的人脸小于200*200个像素，将不能检测出人脸，可以根据需求在100-400间调节大小
        FaceSDKManager.getInstance().getFaceTracker(this).set_min_face_size(200);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isCheckQuality(true);
        // 该角度为商学，左右，偏头的角度的阀值，大于将无法检测出人脸，为了在1：n的时候分数高，注册尽量使用比较正的人脸，可自行条件角度
        FaceSDKManager.getInstance().getFaceTracker(this).set_eulur_angle_thr(45, 45, 45);
        FaceSDKManager.getInstance().getFaceTracker(this).set_isVerifyLive(true);
    }

    @Override
    public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }
        if (v == pickFromAlbumBtn) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);

        } else if (v == submitBtn) {
            // 人脸注册和1：n属于在线接口，需要通过ak，sk获得token后进行调用，此方法为获取token，为了防止你得ak，sk泄露，
            // 建议把此调用放在您的服务端

            if (TextUtils.isEmpty(facePath)) {
                toast("请先进行人脸采集");
            } else {
                // 您可以使用在线活体检测后在进行注册，这样安全性更高，也可以直接注册。（在线活体请在官网控制台提交申请工单）
                reg(facePath);
                // onlineLiveness(facePath);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETECT_FACE && resultCode == Activity.RESULT_OK) {

            facePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
            if (mHeadBmp != null) {
                mHeadBmp.recycle();
            }
            mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
            if (mHeadBmp != null) {
                avatarIv.setImageBitmap(mHeadBmp);
            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            facePath = getRealPathFromURI(uri);

            if (mHeadBmp != null) {
                mHeadBmp.recycle();
            }
            mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
            if (mHeadBmp != null) {
                avatarIv.setImageBitmap(mHeadBmp);
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void reg(String filePath) {
        String username = usernameEt.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(FaceRegActivity.this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        final File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(FaceRegActivity.this, "文件不存在", Toast.LENGTH_LONG).show();
            return;
        }
        // 每个开发者账号只能创建一个人脸库；
        // 每个人脸库下，用户组（group）数量没有限制；
        // 每个用户组（group）下，可添加最多300000张人脸，如每个uid注册一张人脸，则最多300000个用户uid；
        // 每个用户（uid）所能注册的最大人脸数量没有限制；
        // 说明：人脸注册完毕后，生效时间最长为35s，之后便可以进行识别或认证操作。
        // 说明：注册的人脸，建议为用户正面人脸。
        // 说明：uid在库中已经存在时，对此uid重复注册时，新注册的图片默认会追加到该uid下，如果手动选择action_type:replace，
        // 则会用新图替换库中该uid下所有图片。
        // uid          是	string	用户id（由数字、字母、下划线组成），长度限制128B
        // user_info    是	string	用户资料，长度限制256B
        // group_id	    是	string	用户组id，标识一组用户（由数字、字母、下划线组成），长度限制128B。
        // 如果需要将一个uid注册到多个group下，group_id,需要用多个逗号分隔，每个group_id长度限制为48个英文字符
        // image	    是	string	图像base64编码，每次仅支持单张图片，图片编码后大小不超过10M
        // action_type	否	string	参数包含append、replace。如果为“replace”，则每次注册时进行替换replace（新增或更新）操作，
        // 默认为append操作
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                faceReg(file);
            }
        }, 1000);
    }

    private void faceReg(File file) {

        // 用户id（由数字、字母、下划线组成），长度限制128B
        // uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
        String username = usernameEt.getText().toString().trim();

        try {
            String base64Img = new String(Base64.encode(readFile(file), Base64.NO_WRAP));
            JSONObject json = new JSONObject();
            json.put("base64Img", base64Img);
            json.put("username", username);
            json.put("group", Config.GROUP_ID);
            OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/user/register/face")
                    .tag(this)
                    .upJson(json.toJSONString())
                    .execute(new StringCallback() {
                        @Override
                        public void onStart(Request<String, ? extends Request> request) {
                            Log.i("FaceRegActivity", "face reg start");
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i("FaceRegActivity", "orientation->" + response.body());
                            toast("注册成功！");
                            finish();
                        }

                        @Override
                        public void onError(Response<String> response) {
                            JSONObject jsonObject = JSON.parseObject(response.body());
                            toast(jsonObject.getString("message"));
                        }

                        @Override
                        public void onFinish() {
                            Log.i("FaceRegActivity", "face reg finish");
                        }

                        @Override
                        public void uploadProgress(Progress progress) {
                            toast("数据已经发送,请您耐心等待");
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FaceRegActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHeadBmp != null) {
            mHeadBmp.recycle();
        }
    }
}
