/**
 * Copyright (C) 2017 Baidu Inc. All rights reserved.
 */
package com.baidu.idl.face.platform;

import android.content.Context;
import com.baidu.aip.face.stat.Ast;
import com.baidu.idl.face.platform.decode.FaceModule;
import com.baidu.idl.face.platform.strategy.FaceDetectStrategyExtModule;
import com.baidu.idl.face.platform.strategy.FaceLivenessStrategyExtModule;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;

/**
 * FaceSDK功能接口
 */
public class FaceSDKManager {

    private static FaceSDKManager instance = null;
    private Context mContext;
    private FaceTracker mFaceTracker;
    private boolean mInitFlag = false;
    private FaceConfig mFaceConfig = new FaceConfig();

    private FaceSDKManager() {
    }

    public static FaceSDKManager getInstance() {
        if (instance == null) {
            synchronized (FaceSDKManager.class) {
                if (instance == null) {
                    instance = new FaceSDKManager();
                }
            }
        }
        return instance;
    }

    public void initialize(final Context context, String licenseID) {
        initialize(context, licenseID, "", null);
    }

    public void initialize(final Context context, String licenseID, String licenseFileName, FaceTracker faceTracker) {
        mContext = context;
        FaceSDK.initLicense(context, licenseID, licenseFileName, true);
        if (faceTracker == null) {
            mFaceTracker = new FaceTracker(context);
        } else {
            mFaceTracker = faceTracker;
        }
//        FaceSDK.setPerfLogFlag(0);
//        FaceSDK.setValueLogFlag(0);
        FaceSDK.setNumberOfThreads(FaceEnvironment.VALUE_DECODE_THREAD_NUM);
        Ast.getInstance().init(context.getApplicationContext(), "3.3.0.0", "facenormal");
        mInitFlag = true;
    }

    public FaceTracker getFaceTracker() {
        return mFaceTracker;
    }

    public FaceConfig getFaceConfig() {
        return mFaceConfig;
    }

    public void setFaceConfig(FaceConfig config) {
        this.mFaceConfig = config;
        setSDKValue(mFaceConfig);
    }

    private void setSDKValue(FaceConfig options) {
        if (mFaceTracker != null && options != null) {
            mFaceTracker.set_isCheckQuality(options.isCheckFaceQuality);
            mFaceTracker.set_notFace_thr(options.notFaceValue);
            mFaceTracker.set_min_face_size(options.minFaceSize);
            mFaceTracker.set_cropFaceSize(options.cropFaceValue);
            mFaceTracker.set_illum_thr(options.brightnessValue);
            mFaceTracker.set_blur_thr(options.blurnessValue);
            mFaceTracker.set_occlu_thr(options.occlusionValue);
            mFaceTracker.set_isVerifyLive(options.isVerifyLive);
            mFaceTracker.set_max_reg_img_num(options.maxCropImageNum);
            mFaceTracker.set_eulur_angle_thr(
                    options.headPitchValue,
                    options.headYawValue,
                    options.headRollValue
            );
            FaceSDK.setNumberOfThreads(options.faceDecodeNumberOfThreads);
        }
    }

    // 人脸功能
    public IDetect getDetectModule() {
        return new FaceModule(mFaceTracker);
    }

    public ILiveness getLivenessModule() {
        return new FaceModule(mFaceTracker);
    }

    public IDetectStrategy getDetectStrategyModule() {
        FaceDetectStrategyExtModule module =
                new FaceDetectStrategyExtModule(mContext, mFaceTracker);
        module.setConfigValue(mFaceConfig);
        return module;
    }

    public ILivenessStrategy getLivenessStrategyModule() {
        FaceLivenessStrategyExtModule module = new FaceLivenessStrategyExtModule(mContext, mFaceTracker);
        module.setConfigValue(mFaceConfig);
        return module;
    }

    public static boolean isLicenseSuccess() {
        return FaceSDK.getAuthorityStatus() == 0;
    }

    public static String getVersion() {
        return FaceEnvironment.SDK_VERSION;
    }

    // 释放资源
    public static void release() {
        synchronized (FaceSDKManager.class) {
            Ast.getInstance().immediatelyUpload();
            if (instance != null) {
                instance.mInitFlag = false;
                instance.mFaceTracker = null;
                instance.mContext = null;
                instance = null;
            }
        }
    }
}
