package com.formos.huub.mapper.learninglibrary;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.entity.LearningLibraryTag;
import com.formos.huub.domain.entity.Tag;
import com.formos.huub.mapper.tag.TagMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,uses = { TagMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningLibraryTagMapper {

    @Mapping(target = "id.learningLibrary", source = "learningLibrary")
    @Mapping(target = "id.tag", source = "tag")
    LearningLibraryTag toEntity(LearningLibrary learningLibrary, Tag tag);
}
