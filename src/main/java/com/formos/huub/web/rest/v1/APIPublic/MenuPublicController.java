package com.formos.huub.web.rest.v1.APIPublic;

import com.formos.huub.domain.response.menu.IResponseMenu;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.menu.MenuService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/menus")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuPublicController {

    MenuService menuService;

    ResponseSupport responseSupport;

    @GetMapping
    public ResponseEntity<ResponseData> getMenusPublic() {
        List<IResponseMenu> menus = menuService.getMenusPublic();
        return responseSupport.success(ResponseData.builder().data(menus).build());
    }
}

