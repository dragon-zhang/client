package com.letmefold.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Hashtable;

/**
 * 条形码工具类
 *
 * @author success zhang
 * @date 2018.12.13
 */
public class BarCodeUtil {

    /**
     * 生成条形码
     *
     * @param content       条形码中包含的文本信息
     * @param width         生成二维码的宽度,要求>=0(单位:px)
     * @param height        生成二维码的高度,要求>=0(单位:px)
     * @param isShowContent 是否显示下方数字
     * @return Bitmap位图
     * @throws WriterException 创建二维码异常
     */
    public static Bitmap createBarCodeBitmap(String content, int width, int height, boolean isShowContent) throws WriterException {
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
        //字符转码格式设置
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //设置纠错等级，纠错等级越高，能存储的信息越少，纠错等级H>Q>M>L
        hints.put(EncodeHintType.ERROR_CORRECTION, "H");
        //空白边距设置
        hints.put(EncodeHintType.MARGIN, "2");
        //生成二维码矩阵信息
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.EAN_13, width, height, hints);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    // 黑色色块像素设置
                    pixels[y * width + x] = 0xFF000000;
                } else {
                    // 白色色块像素设置
                    pixels[y * width + x] = 0xFFFFFFFF;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        if (isShowContent) {
            bitmap = showContent(bitmap, content);
        }
        return bitmap;
    }

    /**
     * 显示条形的内容
     *
     * @param bCBitmap 已生成的条形码的位图
     * @param content  条形码包含的内容
     * @return 返回生成的新位图, 它是返回的位图与新绘制文本content的组合
     */
    private static Bitmap showContent(Bitmap bCBitmap, String content) {
        if (TextUtils.isEmpty(content) || null == bCBitmap) {
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(0xFF000000);
        paint.setAntiAlias(true);
        //设置填充样式
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.LEFT);
        //测量字符串的宽度
        int textWidth = (int) paint.measureText(content);
        Paint.FontMetrics fm = paint.getFontMetrics();
        //绘制字符串矩形区域的高度
        int textHeight = (int) (fm.bottom - fm.top);
        // x 轴的缩放比率
        int scaleRateX = bCBitmap.getWidth() / textWidth;
        paint.setTextScaleX(scaleRateX);
        //绘制文本的基线
        int baseLine = bCBitmap.getHeight() + textHeight;
        //创建一个图层，然后在这个图层上绘制bCBitmap、content
        Bitmap bitmap = Bitmap.createBitmap(bCBitmap.getWidth(), bCBitmap.getHeight() + 2 * textHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas();
        canvas.drawColor(0xFFFFFFFF);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(bCBitmap, 0, 0, null);
        canvas.drawText(content, bCBitmap.getWidth() / 10f, baseLine, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

}