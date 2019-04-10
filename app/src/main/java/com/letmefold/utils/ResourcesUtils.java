package com.letmefold.utils;

import android.content.Context;
import com.mob.tools.utils.ResHelper;

/**
 * @author yjin
 * @date 2017/5/15
 */
public class ResourcesUtils {
    public static int getBitmapRes(Context context, String resName) {
        return ResHelper.getBitmapRes(context, resName);
    }
}
