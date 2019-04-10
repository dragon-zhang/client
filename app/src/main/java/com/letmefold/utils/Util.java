/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import okio.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author SuccessZhang
 */
public class Util {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static boolean percentEncoded(String encoded, int pos, int limit) {
        return pos + 2 < limit
                && encoded.charAt(pos) == '%'
                && decodeHexDigit(encoded.charAt(pos + 1)) != -1
                && decodeHexDigit(encoded.charAt(pos + 2)) != -1;
    }

    private static int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        return -1;
    }

    /**
     * Returns a substring of {@code input} on the range {@code [pos..limit)} with the following
     * transformations:
     * <ul>
     * <li>Tabs, newlines, form feeds and carriage returns are skipped.
     * <li>In queries, ' ' is encoded to '+' and '+' is encoded to "%2B".
     * <li>Characters in {@code encodeSet} are percent-encoded.
     * <li>Control characters and non-ASCII characters are percent-encoded.
     * <li>All other characters are copied without transformation.
     * </ul>
     *
     * @param alreadyEncoded true to leave '%' as-is; false to convert it to '%25'.
     * @param strict         true to encode '%' if it is not the prefix of a valid percent encoding.
     * @param plusIsSpace    true to encode '+' as "%2B" if it is not already encoded.
     * @param asciiOnly      true to encode all non-ASCII codepoints.
     */
    private static String canonicalize(String input, int pos, int limit, String encodeSet,
                                       boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 0x20
                    || codePoint == 0x7f
                    || codePoint >= 0x80 && asciiOnly
                    || encodeSet.indexOf(codePoint) != -1
                    || codePoint == '%' && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))
                    || codePoint == '+' && plusIsSpace) {
                // Slow path: the character at i requires encoding!
                Buffer out = new Buffer();
                out.writeUtf8(input, pos, i);
                canonicalize(out, input, i, limit, encodeSet, alreadyEncoded, strict, plusIsSpace,
                        asciiOnly);
                return out.readUtf8();
            }
        }

        // Fast path: no characters in [pos..limit) required encoding.
        return input.substring(pos, limit);
    }

    private static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet,
                                     boolean alreadyEncoded, boolean strict, boolean plusIsSpace, boolean asciiOnly) {
        Buffer utf8Buffer = null; // Lazily allocated.
        int codePoint;
        for (int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (alreadyEncoded
                    && (codePoint == '\t' || codePoint == '\n' || codePoint == '\f' || codePoint == '\r')) {
                // TODO Skip this character.
                LogUtil.d(Util.class.getName(), "codePoint:" + codePoint);
                // TODO delete this
            } else if (codePoint == '+' && plusIsSpace) {
                // Encode '+' as '%2B' since we permit ' ' to be encoded as either '+' or '%20'.
                out.writeUtf8(alreadyEncoded ? "+" : "%2B");
            } else if (codePoint < 0x20
                    || codePoint == 0x7f
                    || codePoint >= 0x80 && asciiOnly
                    || encodeSet.indexOf(codePoint) != -1
                    || codePoint == '%' && (!alreadyEncoded || strict && !percentEncoded(input, i, limit))) {
                // Percent encode this character.
                if (utf8Buffer == null) {
                    utf8Buffer = new Buffer();
                }
                utf8Buffer.writeUtf8CodePoint(codePoint);
                while (!utf8Buffer.exhausted()) {
                    int b = utf8Buffer.readByte() & 0xff;
                    out.writeByte('%');
                    out.writeByte(HEX_DIGITS[(b >> 4) & 0xf]);
                    out.writeByte(HEX_DIGITS[b & 0xf]);
                }
            } else {
                // This character doesn't need encoding. Just copy it over.
                out.writeUtf8CodePoint(codePoint);
            }
        }
    }

    static String canonicalize(String input, String encodeSet, boolean alreadyEncoded, boolean strict) {
        return canonicalize(
                input, 0, input.length(), encodeSet, alreadyEncoded, strict, true, true);
    }

    /**
     * 对字符串md5加密
     *
     * @param str
     * @return
     */
    public static String md5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void getCode(String phone, Button btnGetCode, CountDownTimer timer, Context context) {
        //获取验证码
        //判断手机号是否为空并且检查手机号的有效性
        if (!TextUtils.isEmpty(phone) && checkPhoneValid(phone)) {
            //申请验证码，结果都在EventHandler监听返回
            SMSSDK.getVerificationCode("86", phone);
            //禁止按钮的可点击性
            btnGetCode.setEnabled(false);
            timer.start();//开始倒计时
        } else {
            Toast.makeText(context, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查手机号码
     */
    private static boolean checkPhoneValid(String mobiles) {
        String str = "^((1[0-9][0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 获取验证码成功
     */
    private static final int GET_SUCCESS = 1;
    /**
     * 验证成功
     */
    private static final int SUBMIT_SUCCESS = 2;
    /**
     * 检查失败
     */
    private static final int CHECK_FAIL = 3;

    public static void initSMSSDK(final Handler handler) {
        //注册EventHandler监听，每次短信SDK操作回调,
        // 在EventHandler的4个回调方法都可能不在UI线程下，需要使用到消息处理机制。
        SMSSDK.registerEventHandler(new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                super.afterEvent(event, result, data);
                //判断返回的结果
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //服务器返回成功
                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        handler.sendEmptyMessage(GET_SUCCESS);
                    } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //校验成功
                        handler.sendEmptyMessage(SUBMIT_SUCCESS);
                    }
                } else {
                    //服务器返回错误码
                    Throwable throwable = (Throwable) data;
                    Message msg = Message.obtain();
                    msg.what = CHECK_FAIL;
                    msg.obj = throwable;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 设置状态栏沉浸，并且设置状态栏字体与图标的颜色
     */
    public static void immersion(Activity activity, @ColorInt int color, boolean showBlack) {
        //5.0以上的手机
        Window window = activity.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        ViewGroup content = (ViewGroup) activity.findViewById(android.R.id.content);
        ViewGroup childView = (ViewGroup) content.getChildAt(0);
        if (childView != null) {
            childView.setFitsSystemWindows(true);
        }
        window.setStatusBarColor(color);
        if (showBlack) {
            //设置状态栏黑色字体与图标
            QMUIStatusBarHelper.setStatusBarLightMode(activity);
        } else {
            //设置状态栏白色字体与图标
            QMUIStatusBarHelper.setStatusBarDarkMode(activity);
        }
    }

    /**
     * 根据一个网络连接(String)获取bitmap图像
     *
     * @param imageUri
     * @return
     * @throws MalformedURLException
     */
    public static Bitmap getbitmap(String imageUri) {
        // 显示网络上的图片
        Bitmap bitmap;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (OutOfMemoryError | IOException e) {
            e.printStackTrace();
            bitmap = null;
        }
        return bitmap;
    }
}
