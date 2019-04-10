/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.utils;

import com.letmefold.exception.FaceErrorException;

/**
 * @author baidu
 */
public interface OnResultListener<T> {
    void onResult(T result);

    void onError(FaceErrorException error);
}
