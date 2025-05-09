/**
 * ***************************************************
 * * Description :
 * * File        : Charset
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Charset implements CodeEnum {
    UTF_8("UTF-8", "");

    private final String value;
    private final String name;
}
