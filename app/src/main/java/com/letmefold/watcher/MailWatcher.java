package com.letmefold.watcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import com.okgo.OkGo;
import com.okgo.callback.StringCallback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static android.graphics.Color.rgb;

/**
 * @author SuccessZhang
 */
public class MailWatcher implements TextWatcher {

    private TextView textView;

    public MailWatcher(TextView textView) {
        this.textView = textView;
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
        if (s.length() >= 12) {
            OkGo.<String>post("http://email.qhyt1688.com/Home/Vemail")
                    .tag(this)
                    .params("emailpool", s.toString())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(com.okgo.model.Response<String> response) {
                            if (response.isSuccessful()) {
                                String result = response.body();
                                Document doc = Jsoup.parse(result);
                                String key = "此邮箱真实存在";
                                if (!doc.getElementById("emailpool").text().contains(key)) {
                                    textView.setText("邮箱验证失败!");
                                } else {
                                    textView.setTextColor(rgb(102, 153, 0));
                                    textView.setText("邮箱验证成功");
                                }
                            }
                        }
                    });
        } else if (s.length() < 12) {
            textView.setText("邮箱过短!");
        }
    }
}
