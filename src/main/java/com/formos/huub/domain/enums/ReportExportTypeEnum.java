package com.formos.huub.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.formos.huub.framework.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportExportTypeEnum implements CodeEnum {

    BUSINESS_OWNER_INTAKES("BUSINESS_OWNER_INTAKES", "Onboarding_Data"),
    TECHNICAL_ASSISTANCE_APPLICATIONS("TECHNICAL_ASSISTANCE_APPLICATIONS", "Technical_Assistance_Application_Data"),
    APPOINTMENTS("APPOINTMENTS", "Appointment_Data"),
    PROJECTS("PROJECTS", "Project_Data"),
    EVENT_REGISTRATIONS("EVENT_REGISTRATIONS", "Event_Registration_Data"),
    COURSES("COURSES", "Course_Data"),
    TECHNICAL_ASSISTANCE_INVOICE("TECHNICAL_ASSISTANCE_INVOICE", "TA_Invoice_Statement");

    private final String value;

    private final String name;

    @JsonValue
    public String getValue() {
        return value;
    }
}
