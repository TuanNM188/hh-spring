package com.formos.huub.domain.request.translate;

import com.formos.huub.framework.enums.LanguageEnum;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class RequestTranslate {

    @NonNull
    private String text;

    private LanguageEnum sourceLang;

    private LanguageEnum targetLang;
}
