package com.formos.huub.domain.request.userinteraction;

import com.formos.huub.domain.enums.ActionTypeEnum;
import com.formos.huub.domain.enums.ScreenTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestUserInteraction {
    private ScreenTypeEnum screenType;

    private ActionTypeEnum actionType;

    private UUID entryId;

}
