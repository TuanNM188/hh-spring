package com.formos.huub.domain.response.menu;

public interface IResponseMenu {

    String getLabel();

    String getIcon();

    String getRouterLink();

    Integer getPriorityOrder();

    String getPosition();

    Boolean getIsActive();

    String getMenuCode();

    String getMenuKey();

}
