/**
 * ***************************************************
 * * Description :
 * * File        : BooleanUtils
 * * Author      : Hung Tran
 * * Date        : Jan 15, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.utils;

import io.micrometer.common.util.StringUtils;

public class BooleanUtils extends org.apache.commons.lang3.BooleanUtils {

    public static boolean toBoolean(String s) {
        if (StringUtils.isBlank(s) || StringUtils.isEmpty(s)) {
            return false;
        }
        return Boolean.parseBoolean(s);
    }
}
