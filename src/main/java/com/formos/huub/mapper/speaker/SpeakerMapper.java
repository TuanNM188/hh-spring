package com.formos.huub.mapper.speaker;

import com.formos.huub.domain.entity.Speaker;
import com.formos.huub.domain.request.speaker.RequestCreateSpeaker;
import com.formos.huub.domain.request.speaker.RequestUpdateSpeaker;
import com.formos.huub.domain.response.speaker.ResponseSpeakerDetail;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpeakerMapper {

    ResponseSpeakerDetail toResponse(Speaker speaker);

    List<ResponseSpeakerDetail> toListResponse(List<Speaker> speakers);

    Speaker toEntity(RequestCreateSpeaker request);

    Speaker partialUpdate(@MappingTarget Speaker speaker, RequestUpdateSpeaker request);

    Speaker stringToEntity(String id);

}
