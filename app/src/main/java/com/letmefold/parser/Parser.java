/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.letmefold.parser;

import com.letmefold.exception.FaceErrorException;

/**
 * JSON解析
 *
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceErrorException;
}
