/**
 * ***************************************************
 * * Description :
 * * File        : BaseMapper
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.base;

import java.util.List;

public interface BaseMapper<Req, Res, E> {
    /**
     * Entity to Response
     * @param entity /
     * @return /
     */
    Res toResponse(E entity);

    /**
     * Entity list to Response list
     * @param entityList /
     * @return /
     */
    List<Res> toResponseList(List<E> entityList);
}
