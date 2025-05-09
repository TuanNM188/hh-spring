package com.formos.huub.mapper.clientnote;

import com.formos.huub.domain.entity.ClientNote;
import com.formos.huub.domain.request.clientnote.RequestCreateClientNote;
import com.formos.huub.domain.response.clientnote.ResponseClientNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientNoteMapper {

    @Mapping(target = "note", source = "request.note")
    @Mapping(target = "businessOwner.id", source = "businessOwnerId")
    ClientNote toEntity(RequestCreateClientNote request, UUID businessOwnerId);

    ResponseClientNote toResponse(ClientNote clientNote);
}
