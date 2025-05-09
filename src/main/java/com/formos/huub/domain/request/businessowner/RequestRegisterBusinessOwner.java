package com.formos.huub.domain.request.businessowner;

import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestRegisterBusinessOwner {

    private @Valid List<RequestAnswerForm> answers;

    @NotNull
    private Boolean agreeTermAndCondition;
}
