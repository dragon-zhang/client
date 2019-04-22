package com.letmefold.activity.user;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.R;

/**
 * @author SuccessZhang
 * @date 2019.4.22
 */
public class StoreRegisterActivity extends AppCompatActivity {

    private JSONObject userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_register);
        String json = getIntent().getStringExtra("user");
        userInfo = JSON.parseObject(json);
        //todo user_id,店铺名sname，店铺大小size/㎡,店铺地点localtion,经营范围scope
    }

}
