package com.formos.huub.mapper.directmessage;

import com.formos.huub.domain.entity.Conversation;
import com.formos.huub.domain.response.directmessage.ResponseDetailConversation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConversationMapper {

    ResponseDetailConversation toResponse(Conversation conversation);
}
