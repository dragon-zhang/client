/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.parser;

import com.letmefold.exception.FaceErrorException;
import com.letmefold.model.AccessToken;
import com.letmefold.utils.LogUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessTokenParser implements Parser<AccessToken> {
    @Override
    public AccessToken parse(String json) throws FaceErrorException {
        try {
            AccessToken accessToken = new AccessToken();
            accessToken.setJson(json);
            JSONObject jsonObject = new JSONObject(json);
            LogUtil.i("ORC", "json token->" + json);

            if (jsonObject != null) {

                accessToken.setAccessToken(jsonObject.optString("access_token"));
                accessToken.setExpiresIn(jsonObject.optInt("expires_in"));
                return accessToken;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            FaceErrorException error = new FaceErrorException(FaceErrorException.ErrorCode.JSON_PARSE_ERROR, "Json parse error", e);
            throw error;
        }
        return null;
    }
}
