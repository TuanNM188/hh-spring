package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.response.menu.IResponseMenu;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.menu.MenuService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuController {

    MenuService menuService;

    ResponseSupport responseSupport;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_MENUS_BY_CURRENT_USER')")
    public ResponseEntity<ResponseData> getMenusByCurrentUser() {
        List<IResponseMenu> menus = menuService.getMenusByCurrentUser();
        return responseSupport.success(ResponseData.builder().data(menus).build());
    }

}
