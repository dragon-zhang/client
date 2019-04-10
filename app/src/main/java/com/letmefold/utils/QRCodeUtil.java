package com.letmefold.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * @author success zhang
 */
public class QRCodeUtil {

    /**
     * 创建二维码位图
     *
     * @param content 字符串内容(支持中文)
     * @param width   位图宽度(单位:px)
     * @param height  位图高度(单位:px)
     * @return Bitmap
     */
    @Nullable
    public static Bitmap createQRCodeBitmap(String content, int width, int height) throws WriterException {
        return createQRCodeBitmapWithImage(content, width, height, "UTF-8", "H", "2", null, 0);
    }

    /**
     * 生成二维码
     *
     * @param content         二维码中包含的文本信息
     * @param mBitmap         logo图片
     * @param width           生成二维码的宽度,要求>=0(单位:px)
     * @param height          生成二维码的高度,要求>=0(单位:px)
     * @param margin          空白边距 (可修改,要求:整型且>=0), 传null时,zxing源码默认使用"4"
     * @param characterSet    字符集/字符转码格式 (支持格式:{@link CharacterSetECI })。传null时,zxing源码默认使用 "ISO-8859-1"
     * @param errorCorrection 容错级别 (支持级别:{@link ErrorCorrectionLevel })。传null时,zxing源码默认使用 "L"
     * @param innerImageSize  宽度值，影响中间图片大小
     * @return Bitmap位图
     * @throws WriterException 创建二维码异常
     */
    public static Bitmap createQRCodeBitmapWithImage(String content, int width, int height, @Nullable String margin,
                                                     @Nullable String characterSet, @Nullable String errorCorrection,
                                                     Bitmap mBitmap,
                                                     int innerImageSize) throws WriterException {
        //参数合法性判断
        if (TextUtils.isEmpty(content)) {
            // 字符串内容判空
            return null;
        }
        if (width < 0 || height < 0) {
            // 宽和高都需要>=0
            return null;
        }
        Hashtable<EncodeHintType, String> hints = new Hashtable<>();
        if (!TextUtils.isEmpty(characterSet)) {
            //字符转码格式设置
            hints.put(EncodeHintType.CHARACTER_SET, characterSet);
        }
        if (!TextUtils.isEmpty(errorCorrection)) {
            //设置纠错等级，纠错等级越高，能存储的信息越少，纠错等级H>Q>M>L
            hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        }
        if (!TextUtils.isEmpty(margin)) {
            //空白边距设置
            hints.put(EncodeHintType.MARGIN, margin);
        }
        //生成二维码矩阵信息
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        int[] pixels = new int[width * height];
        if (innerImageSize <= 0 || mBitmap == null) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        // 黑色色块像素设置
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        // 白色色块像素设置
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
        } else {
            Matrix m = new Matrix();
            float sx = (float) 2 * innerImageSize / mBitmap.getWidth();
            float sy = (float) 2 * innerImageSize / mBitmap.getHeight();
            //设置缩放信息
            m.setScale(sx, sy);
            //将logo图片按matrix设置的信息缩放
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), m, false);
            int halfW = width / 2;
            int halfH = height / 2;
            //定义数组长度为矩阵高度*矩阵宽度，用于记录矩阵中像素信息
            for (int y = 0; y < height; y++) {
                //从行开始迭代矩阵
                for (int x = 0; x < width; x++) {
                    //迭代列
                    if (x > halfW - innerImageSize && x < halfW + innerImageSize
                            && y > halfH - innerImageSize
                            && y < halfH + innerImageSize) {
                        //该位置用于存放图片信息，记录图片每个像素信息
                        pixels[y * width + x] = mBitmap.getPixel(
                                x - halfW + innerImageSize,
                                y - halfH + innerImageSize);
                    } else if (matrix.get(x, y)) {
                        //如果有黑块点，记录信息
                        pixels[y * width + x] = 0xff000000;
                    }
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}