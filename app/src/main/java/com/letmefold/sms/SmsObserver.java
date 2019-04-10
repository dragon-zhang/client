package com.letmefold.sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author success zhang
 * @date 2018.12.9
 */
public class SmsObserver extends ContentObserver {

    private Context mContext;
    private EditText codeEd;

    public SmsObserver(Handler handler, Context context, EditText editText) {
        super(handler);
        this.mContext = context;
        this.codeEd = editText;
    }

    /**
     * Uri.parse("content://sms/inbox")表示对收到的短信的一个监听的uri.
     */
    @Override
    public void onChange(boolean selfChange) {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        //这里不要使用while循环.我们只需要获取当前发送过来的短信数据就可以了
        if (cursor != null && cursor.moveToNext()) {
            //获取短信内容的实体数据
            sb.append("body=").append(cursor.getString(cursor.getColumnIndex("body")));
            //正则表达式
            String str = "[^0-9]";
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(sb.toString());
            //将输入验证码的控件内容进行改变
            codeEd.setText(matcher.replaceAll(""));
            //关闭游标指针
            cursor.close();
            super.onChange(selfChange);
        }
    }
}