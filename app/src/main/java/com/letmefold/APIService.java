/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold;

import android.content.Context;
import com.letmefold.utils.DeviceUuidFactory;
import com.letmefold.utils.HttpUtil;

/**
 * @author SuccessZhang
 * @date 2018.12.26
 */
public class APIService {

    private String accessToken;

    private String groupId;

    private APIService() {

    }

    private static volatile APIService instance;

    public static APIService getInstance() {
        if (instance == null) {
            synchronized (APIService.class) {
                if (instance == null) {
                    instance = new APIService();


                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        // 采用deviceId分组
        HttpUtil.getInstance().init();
        DeviceUuidFactory.init(context);
    }

    void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
