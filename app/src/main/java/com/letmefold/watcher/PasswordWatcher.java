package com.letmefold.watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.graphics.Color.rgb;

/**
 * @author SuccessZhang
 */
public class PasswordWatcher implements TextWatcher {

    private EditText editText1;
    private Button register;
    private TextView textView;

    public PasswordWatcher(EditText editText1, TextView textView, Button register) {
        this.editText1 = editText1;
        this.textView = textView;
        this.register = register;
    }

    /**
     * 编辑框的内容发生改变之前的回调方法
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * 编辑框的内容正在发生改变时的回调方法 >>用户正在输入
     * 我们可以在这里实时地 通过搜索匹配用户的输入
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * 编辑框的内容改变以后,用户没有继续输入时的回调方法
     */
    @Override
    public void afterTextChanged(Editable s) {
        textView.setTextColor(rgb(204, 0, 0));
        register.setEnabled(false);
        StringBuilder warn = new StringBuilder();
        String pwd1 = editText1.getText().toString();
        if (!pwd1.equals(s.toString())) {
            warn.append("重新输入的密码与原密码不相等!");
        }
        if (s.toString().length() < 6) {
            warn.append("密码长度不能小于6位!");
        }
        if (pwd1.equals(s.toString()) && s.toString().length() >= 6) {
            warn.append("密码验证成功");
            textView.setTextColor(rgb(102, 153, 0));
            register.setEnabled(true);
        }
        textView.setText(warn);
        textView.setVisibility(View.VISIBLE);
    }
}
