package com.letmefold.activity.user;

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
public class StoreRegisterActivity extends AppCompatActivity {

    private JSONObject userInfo;

    private EditText snameEdit;
    private EditText locationEdit;
    private EditText scopeEdit;
    private EditText sizeEdit;
    private QMUIRoundButton sure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_register);
        userInfo = JSON.parseObject(getIntent().getStringExtra("user"));
        findView();
        addListener();
    }

    private void addListener() {
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>(5);
                //user_id,店铺名sname，店铺地点localtion,店铺大小size/㎡,经营范围scope
                map.put("userId", userInfo.get("id"));
                map.put("sname", snameEdit.getText().toString());
                map.put("location", locationEdit.getText().toString());
                map.put("scope", scopeEdit.getText().toString());
                map.put("size", sizeEdit.getText().toString());

                JSONObject json = new JSONObject(map);
                OkGo.<String>post("http://" + IP_AND_PORT + "/rest/v1/store/create")
                        .tag(this)
                        .upJson(json.toJSONString())
                        .execute(new StringCallback() {
                            @Override
                            public void onSuccess(com.okgo.model.Response<String> response) {
                                JSONObject result = JSON.parseObject(response.body());
                                if ("OK".equals(result.getString("msg"))) {
                                    Toast.makeText(StoreRegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                    StoreRegisterActivity.this.finish();
                                }
                            }

                            @Override
                            public void onError(Response<String> response) {
                                JSONObject jsonObject = JSON.parseObject(response.body());
                                Toast.makeText(StoreRegisterActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void findView() {
        snameEdit = (EditText) findViewById(R.id.sname_edit);
        locationEdit = (EditText) findViewById(R.id.location_edit);
        scopeEdit = (EditText) findViewById(R.id.scope_edit);
        sizeEdit = (EditText) findViewById(R.id.size_edit);
        sure = (QMUIRoundButton) findViewById(R.id.sure);
    }

}
