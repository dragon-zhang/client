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
public class CommodityShelvesActivity extends AppCompatActivity {

    private JSONObject userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_shelves);
        String json = getIntent().getStringExtra("user");
        userInfo = JSON.parseObject(json);
        //todo user_id查该用户所拥有的实体店store_id,扫码上货,entity的selled字段置为0
    }

}
