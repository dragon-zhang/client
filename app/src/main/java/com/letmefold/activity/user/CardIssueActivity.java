package com.letmefold.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.letmefold.R;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import com.okgo.model.Response;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.HashMap;
import java.util.Map;

import static com.letmefold.Config.IP_AND_PORT;

/**
 * @author SuccessZhang
 * @date 2019.4.22
 */
public class CardIssueActivity extends AppCompatActivity {

    private JSONObject userInfo;

    private EditText versionEdit;
    private EditText gradeEdit;
    private QMUIRoundButton sure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_issue);
        userInfo = JSON.parseObject(getIntent().getStringExtra("user"));
        findView();
        addListener();
    }

    private void findView() {
        versionEdit = (EditText) findViewById(R.id.version_edit);
        gradeEdit = (EditText) findViewById(R.id.grade_edit);
        sure = (QMUIRoundButton) findViewById(R.id.sure);
    }

    private void addListener() {
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>(5);
                //user_id,发行版本issue_version,发行卡等级grade(数组，如"金","银","铜")
                map.put("userId", userInfo.get("id"));
                map.put("issueVersion", versionEdit.getText().toString());
                map.put("grade", gradeEdit.getText().toString());
                JSONObject json = new JSONObject(map);
                OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/card/create")
                        .tag(this)
                        .upJson(json.toJSONString())
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(com.okgo.model.Response<String> response) {
                                JSONObject result = JSON.parseObject(response.body());
                                if ("OK".equals(result.getString("msg"))) {
                                    Toast.makeText(CardIssueActivity.this, "发行成功", Toast.LENGTH_SHORT).show();
                                    Intent resultIntent = new Intent();
                                    CardIssueActivity.this.setResult(RESULT_OK, resultIntent);
                                    CardIssueActivity.this.finish();
                                }
                            }

                            @Override
                            public void onError(Response<String> response) {
                                JSONObject jsonObject = JSON.parseObject(response.body());
                                Toast.makeText(CardIssueActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
