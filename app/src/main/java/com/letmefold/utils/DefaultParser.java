/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.utils;

import android.util.Log;
import com.letmefold.exception.FaceErrorException;
import com.letmefold.model.ResponseResult;
import com.letmefold.parser.Parser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author baidu
 */
public class DefaultParser implements Parser<ResponseResult> {

    @Override
    public ResponseResult parse(String json) throws FaceErrorException {
        Log.e("xx", "DefaultParser:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceErrorException error = new FaceErrorException(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                throw error;
            }

            ResponseResult result = new ResponseResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceErrorException error = new FaceErrorException(FaceErrorException.ErrorCode.JSON_PARSE_ERROR, "Json parse error:" + json, e);
            throw error;
        }
    }
}
