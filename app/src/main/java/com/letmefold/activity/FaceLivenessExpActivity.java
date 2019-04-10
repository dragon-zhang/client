package com.letmefold.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceLivenessActivity;
import com.letmefold.widget.DefaultDialog;

import java.util.HashMap;

/**
 * @author baidu
 */
public class FaceLivenessExpActivity extends FaceLivenessActivity {

    private DefaultDialog mDefaultDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onLivenessCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onLivenessCompletion(status, message, base64ImageMap);
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            Intent intent = new Intent(FaceLivenessExpActivity.this, FaceRegActivity.class);
            intent.putExtra("isLive", "live");
            startActivity(intent);
            finish();
        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
            if (mDefaultDialog == null) {
                DefaultDialog.Builder builder = new DefaultDialog.Builder(this);
                builder.setTitle("活体检测").
                        setMessage("采集超时").
                        setNegativeButton("确认",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDefaultDialog.dismiss();
                                        finish();
                                    }
                                });
                mDefaultDialog = builder.create();
                mDefaultDialog.setCancelable(true);
            }
            mDefaultDialog.dismiss();
            mDefaultDialog.show();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

}
