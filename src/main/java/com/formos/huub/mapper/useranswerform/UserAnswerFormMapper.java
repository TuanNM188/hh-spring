package com.formos.huub.mapper.useranswerform;

import com.formos.huub.domain.entity.UserAnswerForm;
import com.formos.huub.domain.enums.EntryTypeEnum;
import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import com.formos.huub.domain.response.answerform.ResponseUserAnswerForm;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserAnswerFormMapper {

    @Mapping(target = "entryId", source = "entryId")
    @Mapping(target = "entryType", source = "entryType")
    UserAnswerForm toEntity(RequestAnswerForm request, FormCodeEnum formCode, UUID entryId, EntryTypeEnum entryType);

    ResponseUserAnswerForm toResponse( final UserAnswerForm userAnswerForm);
}
