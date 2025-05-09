/**
 * ***************************************************
 * * Description :
 * * File        : BaseValidator
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.base;

import com.formos.huub.framework.support.ValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseValidator<T> {

    @Autowired
    protected ValidationSupport validationSupport;

    /**
     * initialize method
     *
     * @param constraintAnnotation
     */
    public void initialize(final T constraintAnnotation) {
        // no implement
    }
}
