package com.formos.huub.framework.service.translate;

import com.formos.huub.framework.enums.LanguageEnum;

public interface ITranslateService {
    String translate(String text, LanguageEnum targetLang, LanguageEnum sourceLang);
}
