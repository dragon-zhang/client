
/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.parser;

import android.util.Log;
import com.letmefold.exception.FaceErrorException;
import com.letmefold.model.OnlineFaceliveResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OnlineLivenessResultParser implements Parser<OnlineFaceliveResult> {

    @Override
    public OnlineFaceliveResult parse(String json) throws FaceErrorException {

        Log.i("OnlineFaceliveResult", "OnlineFaceliveResult->" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceErrorException error = new FaceErrorException(jsonObject.optInt("error_code"),
                        jsonObject.optString("error_msg"));
                throw error;
            }

            OnlineFaceliveResult livenessResult = new OnlineFaceliveResult();
            livenessResult.setLogId(jsonObject.optLong("log_id"));
            livenessResult.setJsonRes(json);

            JSONArray results = jsonObject.optJSONArray("result");

            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject livenessJOSNObject = results.optJSONObject(i);
                    if (livenessJOSNObject != null) {
                        double faceliveness = livenessJOSNObject.optDouble("faceliveness");
                        livenessResult.getFacelivenessValue().add(faceliveness);
                    }
                }
            }

            return livenessResult;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceErrorException error = new FaceErrorException(FaceErrorException.ErrorCode.JSON_PARSE_ERROR,
                    "Json parse error:" + json, e);
            throw error;
        }
    }
}
