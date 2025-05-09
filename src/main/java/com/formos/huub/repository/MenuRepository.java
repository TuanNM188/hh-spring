package com.formos.huub.repository;

import com.formos.huub.domain.entity.Menu;
import com.formos.huub.domain.response.menu.IResponseMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    @Query("SELECT m.label AS label, m.icon AS icon, m.routerLink AS routerLink, m.priorityOrder AS priorityOrder, " +
        "m.position AS position, m.isActive AS isActive, m.menuCode AS menuCode, m.menuKey AS menuKey " +
        "FROM User u " +
        "JOIN u.authorities a " +
        "JOIN a.menus m " +
        "WHERE u.login = :login AND m.isActive IS true " +
        "GROUP BY m.id "
    )
    List<IResponseMenu> getMenusByCurrentUser(String login);

    @Query(
        "SELECT m.label AS label, m.icon AS icon, m.routerLink AS routerLink, m.priorityOrder AS priorityOrder, m.position AS position, m.menuKey AS menuKey " +
            "FROM Menu m " +
            "WHERE m.isPublic is true AND m.isActive IS true"
    )
    List<IResponseMenu> getMenusPublic();

}
