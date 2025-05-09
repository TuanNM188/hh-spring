package com.formos.huub.domain.request.portals;

import com.formos.huub.domain.request.useranswerform.RequestAnswerForm;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSubmitTechnicalAssistance {
    private @Valid List<RequestAnswerForm> answers;
}
