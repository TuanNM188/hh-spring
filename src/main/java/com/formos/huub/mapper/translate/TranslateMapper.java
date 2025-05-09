package com.formos.huub.mapper.translate;

import com.formos.huub.domain.entity.Translate;
import com.formos.huub.domain.request.translate.RequestTranslate;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TranslateMapper {
    @Mapping(target = "sourceContent", source = "request.text")
    @Mapping(target = "targetContent", source = "translatedText")
    @Mapping(target = "sourceHash", expression = "java(com.formos.huub.framework.utils.StringUtils.generateChecksum(request.getText()))")
    @Mapping(target = "sourceLanguage", source = "request.sourceLang")
    @Mapping(target = "targetLanguage", source = "request.targetLang")
    Translate toEntity(RequestTranslate request, String translatedText);
}
