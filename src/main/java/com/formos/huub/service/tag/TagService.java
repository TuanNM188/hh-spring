package com.formos.huub.service.tag;

import com.formos.huub.domain.response.tag.ResponseTagMeta;
import com.formos.huub.mapper.tag.TagMapper;
import com.formos.huub.repository.TagRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagService {

    TagRepository tagRepository;

    TagMapper tagMapper;

    public List<ResponseTagMeta> getAll(){
        return tagRepository.findAll().stream().map(tagMapper::toResponse).toList();
    }
}
