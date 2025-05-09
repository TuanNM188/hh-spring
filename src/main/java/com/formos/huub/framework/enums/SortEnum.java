/**
 * ***************************************************
 * * Description :
 * * File        : SortEnum
 * * Author      : Hung Tran
 * * Date        : Mar 01, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortEnum implements CodeEnum {
    DESC("DESC", ""),
    ASC("ASC", "");

    private final String value;
    private final String name;
}
