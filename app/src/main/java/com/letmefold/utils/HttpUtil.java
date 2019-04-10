/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.letmefold.APIService;
import com.letmefold.exception.FaceErrorException;
import com.letmefold.model.AccessToken;
import com.letmefold.model.RequestParams;
import com.letmefold.parser.AccessTokenParser;
import com.letmefold.parser.Parser;
import okhttp3.*;
import org.jsoup.helper.StringUtil;

import java.io.IOException;

import static com.letmefold.DemoApplication.mAccessToken;
import static com.letmefold.DemoApplication.sinaUserInfo;

/**
 * @author baidu
 * 使用okhttp请求token和调用服务
 */
public class HttpUtil {

    private OkHttpClient client;
    private Handler handler;
    private static volatile HttpUtil instance;

    public static Request buildRequest(String url, String json) {
        //创建一个Request
        return new Request.Builder()
                .header("Content-Type", "application/json;charset=UTF-8")
                .post(RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json))
                .url(url)
                .build();
    }

    public static void initSinaUserInfoAndLogin(final Handler mHandler) {
        OkHttpClient client = new OkHttpClient();
        if (!StringUtil.isBlank(mAccessToken.getToken())) {
            String getUrl = "https://api.weibo.com/2/users/show.json?" +
                    "access_token=" + mAccessToken.getToken() +
                    //uid可以唯一标识一个微博用户
                    "&uid=" + mAccessToken.getUid();
            //创建一个Request
            Request request = new Request.Builder()
                    .get()
                    //获取微博用户信息
                    .url(getUrl)
                    .build();
            //通过client发起请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        sinaUserInfo = JSON.parseObject(result);
                        sinaUserInfo.put("register_type", "sina");
                        Message msg = new Message();
                        msg.what = 0;
                        mHandler.sendMessage(msg);
                    }
                }
            });
        }
    }

    private HttpUtil() {
    }

    public static HttpUtil getInstance() {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }
        return instance;
    }

    public void init() {
        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
    }

    public <T> void post(String path, RequestParams params, final Parser<T> parser, final OnResultListener<T>
            listener) {
        post(path, "images", params, parser, listener);
    }

    public <T> void post(String path, String key, RequestParams params,
                         final Parser<T> parser, final OnResultListener<T> listener) {
        Base64RequestBody body = new Base64RequestBody();

        body.setKey(key);
        body.setFileParams(params.getFileParams());
        body.setStringParams(params.getStringParams());
        body.setJsonParams(params.getJsonParams());

        final Request request = new Request.Builder()
                .url(path)
                .post(body)
                .build();

        if (client == null) {
            FaceErrorException err = new FaceErrorException(-999, "okhttp inner error");
            listener.onError(err);
            return;
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                final FaceErrorException error = new FaceErrorException(FaceErrorException.ErrorCode.NETWORK_REQUEST_ERROR,
                        "network request error", e);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(error);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                Log.d("wtf", "onResponse json->" + responseString);
                final T result;
                try {
                    result = parser.parse(responseString);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResult(result);
                        }
                    });
                } catch (final FaceErrorException faceError) {
                    faceError.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(faceError);
                        }
                    });
                }

            }
        });
    }

    public void getAccessToken(final OnResultListener<AccessToken> listener, String url, String param) {

        final AccessTokenParser accessTokenParser = new AccessTokenParser();
        RequestBody body = RequestBody.create(MediaType.parse("text/html"), param);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                FaceErrorException error = new FaceErrorException(FaceErrorException.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                listener.onError(error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response == null || response.body() == null || TextUtils.isEmpty(response.toString())) {
                    throwError(listener, FaceErrorException.ErrorCode.ACCESS_TOKEN_PARSE_ERROR,
                            "token is parse error, please rerequest token");
                }
                try {
                    AccessToken accessToken = accessTokenParser.parse(response.body().string());
                    if (accessToken != null) {
                        APIService.getInstance().setAccessToken(accessToken.getAccessToken());
                        listener.onResult(accessToken);
                    } else {
                        throwError(listener, FaceErrorException.ErrorCode.ACCESS_TOKEN_PARSE_ERROR,
                                "token is parse error, please rerequest token");
                    }
                } catch (FaceErrorException error) {
                    error.printStackTrace();
                    listener.onError(error);
                }
            }
        });

    }

    /**
     * throw error
     *
     * @param errorCode
     * @param msg
     * @param listener
     */
    private static void throwError(OnResultListener<AccessToken> listener, int errorCode, String msg) {
        FaceErrorException error = new FaceErrorException(errorCode, msg);
        listener.onError(error);
    }

    /**
     * 释放资源
     */
    public void release() {
        client = null;
        handler = null;
    }
}
