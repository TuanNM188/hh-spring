package com.formos.huub.domain.response.calendarintegrate;

import java.util.List;

/**
 * ***************************************************
 * * Description :
 * * File        : AbstractPageableResponse
 * * Author      : Hung Tran
 * * Date        : Feb 13, 2025
 * ***************************************************
 **/

public abstract class AbstractResponsePageable<T> {

    private ResponsePageable pagination;

    public abstract List<T> getItems();

    public ResponsePageable getPagination() {
        return pagination;
    }

    public void setPagination(ResponsePageable pagination) {
        this.pagination = pagination;
    }
}
