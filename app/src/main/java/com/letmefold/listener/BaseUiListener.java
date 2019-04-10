package com.letmefold.listener;

import android.content.Context;
import android.widget.Toast;
import com.letmefold.DemoApplication;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import org.json.JSONObject;

/**
 * @author SuccessZhang
 */
public class BaseUiListener implements IUiListener {

    private Context context;

    protected BaseUiListener(Context context) {
        this.context = context;
    }

    @Override
    public void onComplete(Object response) {
        if (null == response) {
            Toast.makeText(context, "返回为空,登录失败", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject jsonResponse = (JSONObject) response;
        if (jsonResponse.length() == 0) {
            Toast.makeText(context, "返回为空,登录失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
        doComplete((JSONObject) response);
    }

    protected void doComplete(JSONObject values) {

    }

    @Override
    public void onError(UiError e) {
        Toast.makeText(context, "onError: " + e.errorDetail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel() {
        Toast.makeText(context, "onCancel", Toast.LENGTH_SHORT).show();
        if (DemoApplication.isServerSideLogin) {
            DemoApplication.isServerSideLogin = false;
        }
    }
}
