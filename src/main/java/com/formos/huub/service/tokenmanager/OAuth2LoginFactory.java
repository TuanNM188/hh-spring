package com.formos.huub.service.tokenmanager;

import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginFactory {

    private final Map<String, OAuth2LoginStrategy> strategies;

    public OAuth2LoginFactory(List<OAuth2LoginStrategy> strategyList) {
        strategies = strategyList
            .stream()
            .collect(Collectors.toMap(s -> s.getClass().getSimpleName().replace("LoginStrategy", "").toUpperCase(), s -> s));
    }

    public OAuth2LoginStrategy getStrategy(String provider) {
        return Optional.ofNullable(strategies.get(provider.toUpperCase())).orElseThrow(
            () -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0096, provider))
        );
    }
}
