/**
 * ***************************************************
 * * Description :
 * * File        : ProjectAttachmentMapper
 * * Author      : Hung Tran
 * * Date        : Feb 05, 2025
 * ***************************************************
 **/
package com.formos.huub.mapper.entityattachment;

import com.formos.huub.domain.entity.EntityAttachment;
import com.formos.huub.domain.request.common.RequestAttachmentFile;
import com.formos.huub.domain.response.attachment.ResponseAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EntityAttachmentMapper {
    @Mapping(target = "type", source = "request.contentType")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "realName", source = "request.realName")
    @Mapping(target = "size", source = "request.size")
    @Mapping(target = "path", source = "request.url")
    @Mapping(target = "icon", source = "request.icon")
    @Mapping(target = "entityType", source = "request.entityType")
    @Mapping(target = "suffix", expression = "java(com.formos.huub.framework.utils.FileUtils.getExtensionName(request.getName()))")
    @Mapping(target = "entityId", source = "id")
    EntityAttachment toEntity(RequestAttachmentFile request, UUID id);

    EntityAttachment toResponse(EntityAttachment projectAttachment);

    ResponseAttachment toResponseAttachment(EntityAttachment entityAttachment);

    List<ResponseAttachment> toResponseAttachments(List<EntityAttachment> entityAttachment);
}
