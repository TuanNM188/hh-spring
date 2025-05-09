package com.formos.huub.domain.request.useranswerform;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestBusinessOwnerAnswerForm {

    @Valid
    private List<RequestAnswerForm> businessOwner;

}
