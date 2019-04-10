/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.parser;

import android.util.Log;
import com.letmefold.exception.FaceErrorException;
import com.letmefold.model.RegResult;
import org.json.JSONException;
import org.json.JSONObject;

public class RegResultParser implements Parser<RegResult> {


    @Override
    public RegResult parse(String json) throws FaceErrorException {
        Log.e("xx", "oarse:" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceErrorException error = new FaceErrorException(jsonObject.optInt("error_code"), jsonObject.optString("error_msg"));
                if (error.getErrorCode() != 0) {
                    throw error;
                }
            }

            RegResult result = new RegResult();
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
