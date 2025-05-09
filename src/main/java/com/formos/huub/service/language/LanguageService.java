package com.formos.huub.service.language;

import com.formos.huub.domain.entity.Language;
import com.formos.huub.domain.request.language.RequestCreateLanguage;
import com.formos.huub.domain.request.language.RequestUpdateLanguages;
import com.formos.huub.domain.response.language.ResponseLanguage;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.repository.LanguageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LanguageService {

    LanguageRepository languageRepository;

    public List<ResponseLanguage> getAll() {
        return languageRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().map(ele -> {
            var item = new ResponseLanguage();
            BeanUtils.copyProperties(ele, item);
            return item;
        }).toList();
    }

    /**
     * create Languages
     *
     * @param request RequestCreateLanguage
     */
    public ResponseLanguage createLanguages(RequestCreateLanguage request) {
        var isExistLanguage = languageRepository.existsByName(request.getName());
        if (isExistLanguage) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "language"));
        }
        var language = new Language();
        BeanUtils.copyProperties(request, language);
        var languages = languageRepository.save(language);
        var response = new ResponseLanguage();
        BeanUtils.copyProperties(languages, response);
        return response;
    }

    /**
     * update Languages
     *
     * @param id      UUID
     * @param request RequestUpdateLanguages
     */
    public ResponseLanguage updateLanguages(UUID id, RequestUpdateLanguages request) {
        var languages = languageRepository.findById(id).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "language"))
        );
        var isExistLanguage = languageRepository.existsByNameAndNotEqualId(request.getName(), id);
        if (isExistLanguage) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "language"));
        }
        BeanUtils.copyProperties(request, languages);
        languages = languageRepository.save(languages);
        var response = new ResponseLanguage();
        BeanUtils.copyProperties(languages, response);
        return response;
    }

    /**
     * delete Languages
     *
     * @param id UUID
     */
    public void deleteLanguages(UUID id) {
        var language = languageRepository.findById(id).orElseThrow(
            () -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "language"))
        );
        languageRepository.delete(language);
    }
}
