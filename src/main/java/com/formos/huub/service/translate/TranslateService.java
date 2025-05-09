package com.formos.huub.service.translate;

import com.formos.huub.domain.entity.Translate;
import com.formos.huub.domain.request.translate.RequestTranslate;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.enums.LanguageEnum;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.translate.AwsTranslateService;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.translate.TranslateMapper;
import com.formos.huub.repository.TranslateRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TranslateService {

    AwsTranslateService awsTranslateService;

    TranslateRepository translateRepository;

    TranslateMapper translateMapper;

    /**
     * translate text
     *
     * @param requestTranslate RequestTranslate
     * @return String
     */
    public String translate(RequestTranslate requestTranslate) {
        setDefaultLanguagesIfMissing(requestTranslate);
        validateLanguages(requestTranslate.getSourceLang(), requestTranslate.getTargetLang());
        var sourceText = requestTranslate.getText();
        if (StringUtils.isEmpty(sourceText)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "text"));
        }
        return retrieveOrCreateTranslation(requestTranslate);
    }

    private String retrieveOrCreateTranslation(RequestTranslate requestTranslate) {
        var sourceText = requestTranslate.getText();
        String checksumString = StringUtils.generateChecksum(sourceText);
        Optional<Translate> existingTranslation = findAndCleanupTranslations(
            requestTranslate.getSourceLang(),
            requestTranslate.getTargetLang(),
            checksumString
        );
        return existingTranslation.map(Translate::getTargetContent).orElseGet(() -> translateAndSave(requestTranslate));
    }

    private String translateAndSave(RequestTranslate requestTranslate) {
        try {
            String translatedText = awsTranslateService.translate(
                requestTranslate.getText(),
                requestTranslate.getTargetLang(),
                requestTranslate.getSourceLang()
            );

            Translate newTranslate = translateMapper.toEntity(requestTranslate, translatedText);
            translateRepository.save(newTranslate);
            return translatedText;
        } catch (Exception e) {
            log.error("Failed to translate and save text: {}", requestTranslate.getText(), e);
            return requestTranslate.getText();
        }
    }

    private Optional<Translate> findAndCleanupTranslations(LanguageEnum sourceLang, LanguageEnum targetLang, String checksumString) {
        List<Translate> translations = translateRepository.findBySourceLanguageAndTargetLanguageAndSourceHash(
            sourceLang,
            targetLang,
            checksumString
        );
        if (translations.isEmpty()) {
            return Optional.empty();
        }
        Translate latestTranslation = translations.getLast();
        // Cleanup duplicate translations if exist
        if (translations.size() > 1) {
            List<UUID> idsToDelete = translations
                .stream()
                .limit(translations.size() - 1)
                .map(Translate::getId)
                .collect(Collectors.toList());
            translateRepository.deleteAllById(idsToDelete);
        }
        return Optional.of(latestTranslation);
    }

    private void validateLanguages(LanguageEnum targetLang, LanguageEnum sourceLang) {
        if (targetLang.equals(sourceLang)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0077));
        }
    }

    private void setDefaultLanguagesIfMissing(RequestTranslate requestTranslate) {
        if (Optional.ofNullable(requestTranslate.getSourceLang()).isEmpty()) {
            requestTranslate.setSourceLang(LanguageEnum.valueOf(AppConstants.DEFAULT_TRANSLATE_SOURCE_LANG.toUpperCase()));
        }
        if (Optional.ofNullable(requestTranslate.getTargetLang()).isEmpty()) {
            requestTranslate.setTargetLang(LanguageEnum.valueOf(AppConstants.DEFAULT_LANGUAGE.toUpperCase()));
        }
    }
}
