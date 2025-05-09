package com.formos.huub.service.userinteractions;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.entity.UserInteractions;
import com.formos.huub.domain.request.userinteraction.RequestUserInteraction;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.enums.LanguageEnum;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.repository.UserInteractionsRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UserInteractionService {

    UserInteractionsRepository userInteractionsRepository;

    UserRepository userRepository;

    public void saveUserInteraction(RequestUserInteraction request) {
        var currentUser = getCurrentUser();
        if (Objects.isNull(currentUser)) {
            return;
        }
        var portalId = PortalContextHolder.getPortalId();
        var userInteractions = UserInteractions.builder()
            .userId(currentUser.getId())
            .portalId(portalId)
            .screenType(request.getScreenType())
            .actionType(request.getActionType())
            .entryId(request.getEntryId())
            .build();
        userInteractionsRepository.save(userInteractions);
    }

    public String saveUserPreferredLanguage(String languageCode) {
        LanguageEnum language = validateAndGetLanguage(languageCode);
        User currentUser = getCurrentUser();
        currentUser.setLangKey(language.getValue());
        return userRepository.save(currentUser).getLangKey();
    }

    private LanguageEnum validateAndGetLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0079));
        }
        try {
            LanguageEnum language = LanguageEnum.valueOf(languageCode.toUpperCase());
            return LanguageEnum.AUTO.equals(language) ? LanguageEnum.EN : language;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0079));
        }
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin).orElse(null);
    }
}
