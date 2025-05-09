package com.formos.huub.domain.request.bookingsetting;

import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestConnectCalendar {


    @EnumCheck(enumClass = CalendarStatusEnum.class)
    private CalendarTypeEnum calendarType;

    @EnumCheck(enumClass = CalendarStatusEnum.class)
    private CalendarStatusEnum calendarStatus;

    private String calLink;

    private String accessToken;

    private String refreshToken;

    private Long expireDate;

    private String calendarRefId;

    private Boolean isNewToken;

    private String email;

}
