package com.letmefold.listener;

import android.content.Context;
import android.widget.Toast;
import com.letmefold.R;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;

import static com.letmefold.DemoApplication.mAccessToken;

/**
 * @author SuccessZhang
 * @date 2018.12.26
 */
public class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener {

    private Context context;

    public SelfWbAuthListener(Context context) {
        this.context = context;
    }

    @Override
    public void onSuccess(final Oauth2AccessToken token) {
        mAccessToken = token;
        if (mAccessToken.isSessionValid()) {
            //保存Token到SharedPreferences
            AccessTokenKeeper.writeAccessToken(context, mAccessToken);
            Toast.makeText(context, R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void cancel() {
        Toast.makeText(context, R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFailure(WbConnectErrorMessage errorMessage) {
        Toast.makeText(context, errorMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
    }
}
