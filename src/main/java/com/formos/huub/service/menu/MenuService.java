package com.formos.huub.service.menu;

import com.formos.huub.domain.constant.MenuConstant;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.response.menu.IResponseMenu;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.helper.portal.PortalHelper;
import com.formos.huub.repository.MenuRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.portals.PortalFormService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.formos.huub.config.Constants.SYSTEM;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuService extends BaseService {

    MenuRepository menuRepository;

    PortalFormService portalFormService;

    PortalHelper portalHelper;

    /**
     * Get menus by current user
     *
     * @return List<IResponseMenu>
     */
    public List<IResponseMenu> getMenusByCurrentUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElse(SYSTEM);
        List<IResponseMenu> menus = menuRepository.getMenusByCurrentUser(login);

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.BUSINESS_OWNER)) {
            filterMenusForBusinessOwner(menus);
        }

        return menus;
    }

    private void filterMenusForBusinessOwner(List<IResponseMenu> menus) {
        UUID portalId = PortalContextHolder.getPortalId();
        if (Objects.isNull(portalId)) {
            return;
        }
        Portal portal = portalHelper.getPortal(portalId);
        ApprovalStatusEnum applicationStatus = portalFormService.getSubmitTechnicalAssistanceStatus(portal);

        String menuToRemove = ApprovalStatusEnum.APPROVED.equals(applicationStatus)
            ? MenuConstant.APPLY_FOR_1_1_SUPPORT
            : MenuConstant.MANAGE_FOR_1_1_SUPPORT;

        menus.removeIf(menu -> menuToRemove.equals(menu.getMenuCode()));
    }

    public List<IResponseMenu> getMenusPublic() {
        return menuRepository.getMenusPublic();
    }

}
