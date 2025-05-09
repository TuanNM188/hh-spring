package com.formos.huub.mapper.tag;

import com.formos.huub.domain.entity.Tag;
import com.formos.huub.domain.response.tag.ResponseTagMeta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {

    @Mapping(target = "name", source = "tagName")
    Tag toEntity(String tagName);

    ResponseTagMeta toResponse(Tag tag);

}
