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
public class CardIssueActivity extends AppCompatActivity {

    private JSONObject userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_issue);
        String json = getIntent().getStringExtra("user");
        userInfo = JSON.parseObject(json);
        //todo user_id,发行版本issue_version,发行卡等级grade(数组，如"金","银","铜")
    }

}
