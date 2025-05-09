/**
 * ***************************************************
 * * Description :
 * * File        : EnumUtils
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.utils;

import com.formos.huub.framework.enums.CodeEnum;
import org.apache.commons.lang3.StringUtils;

public class EnumUtils {

    /**
     * check enum is valid or not.
     *
     * @param enumClass enum class
     * @param target value
     * @return string
     */
    public static boolean isValidEnum(final Class<? extends CodeEnum> enumClass, final String target) {
        final CodeEnum[] enumValues = enumClass.getEnumConstants();

        boolean result = false;
        for (final CodeEnum codeEnum : enumValues) {
            if (StringUtils.equals(target, codeEnum.getValue())) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * check enum is valid or not.
     *
     * @param enumClass enum class
     * @param target value
     * @return boolean
     */
    public static CodeEnum getEnum(final Class<? extends CodeEnum> enumClass, final String target) {
        final CodeEnum[] enumValues = enumClass.getEnumConstants();

        for (final CodeEnum codeEnum : enumValues) {
            if (StringUtils.equals(target, codeEnum.getValue())) {
                return codeEnum;
            }
        }
        return null;
    }
}
