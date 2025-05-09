package com.formos.huub.domain.constant;

import com.formos.huub.domain.enums.ApprovalStatusEnum;

import java.util.HashMap;
import java.util.Map;

public final class ActiveCampaignConstant {

    public static final String FIELD_FIRSTNAME = "FIRSTNAME";
    public static final String FIELD_LASTNAME = "LASTNAME";
    public static final String FIELD_EMAIL = "EMAIL";
    public static final String FIELD_PHONE = "PHONE";

    // HUUB v2 custom fields
    public static final String FIELD_PORTAL_DOMAIN_V2 = "PORTAL_DOMAIN_V2";
    public static final String FIELD_PORTAL_NAME_V2 = "PORTAL_NAME_V2";
    public static final String FIELD_BUSINESS_INDUSTRY_V2 = "BUSINESS_INDUSTRY_V2";
    public static final String FIELD_BUSINESS_OPEN_DATE_V2 = "BUSINESS_OPEN_DATE_V2";
    public static final String FIELD_NUMBER_OF_EMPLOYEES_V2 = "NUMBER_OF_EMPLOYEES_V2";
    public static final String FIELD_GENDER_IDENTITY_V2 = "GENDER_IDENTITY_V2";
    public static final String FIELD_PREFERRED_LANGUAGE_V2 = "PREFERRED_LANGUAGE_V2";
    public static final String FIELD_ETHNICITY_V2 = "ETHNICITY_V2";
    public static final String FIELD_MILITARY_AFFILIATION_V2 = "MILITARY_AFFILIATION_V2";
    public static final String FIELD_LGBTQIA_IDENTIFICATION_V2 = "LGBTQIA_IDENTIFICATION_V2";
    public static final String FIELD_1ST_PRIORITY_AREA_V2 = "1ST_PRIORITY_AREA_V2";
    public static final String FIELD_2ND_PRIORITY_AREA_V2 = "2ND_PRIORITY_AREA_V2";
    public static final String FIELD_SELECTED_RESOURCES_V2 = "SELECTED_RESOURCES_V2";
    public static final String FIELD_TA_STATUS_V2 = "TA_STATUS_V2";
    public static final String FIELD_APPLICATION_DATE_V2 = "APPLICATION_DATE_V2";
    public static final String FIELD_ASSIGNED_VENDOR_V2 = "ASSIGNED_VENDOR_V2";
    public static final String FIELD_AWARD_AMOUNT_V2 = "AWARD_AMOUNT_V2";
    public static final String FIELD_ADVISOR_1_V2 = "ADVISOR_1_V2";
    public static final String FIELD_ADVISOR_2_V2 = "ADVISOR_2_V2";
    public static final String FIELD_SCHEDULED_APPOINTMENT_COUNT_V2 = "SCHEDULED_APPOINTMENT_COUNT_V2";
    public static final String FIELD_COURSE_COUNT_V2 = "COURSE_COUNT_V2";
    public static final String FIELD_LAST_LOGIN_V2 = "LAST_LOGIN_V2";

    // HUUB v2 tags
    public static final String TAG_HUUB_REGISTERED_V2 = "HUUB Registered v2";
    public static final String TAG_HUUB_UNSUPPORTED_ADDRESS_V2 = "HUUB Unsupported Address v2";

    // HUUB v2 Technical Assistance status
    public static final String APPLIED = "Applied";
    public static final String APPROVED = "Approved";
    public static final String DENIED = "Denied";
    public static final String COMPLETED = "Completed";

    public static Map<String, String> TA_STATUS = new HashMap<>();
    static {
        TA_STATUS.put(ApprovalStatusEnum.SUBMITTED.getValue(), ActiveCampaignConstant.APPLIED);
        TA_STATUS.put(ApprovalStatusEnum.VENDOR_ASSIGNED.getValue(), ActiveCampaignConstant.APPLIED);
        TA_STATUS.put(ApprovalStatusEnum.APPROVED.getValue(), ActiveCampaignConstant.APPROVED);
        TA_STATUS.put(ApprovalStatusEnum.DENIED.getValue(), ActiveCampaignConstant.DENIED);
        TA_STATUS.put(ApprovalStatusEnum.EXPIRED.getValue(), ActiveCampaignConstant.COMPLETED);
    }

    public static Map<String, String> FIELD_UNSUPPORTED_ADDRESS_MAP = new HashMap<>();
    static {
        FIELD_UNSUPPORTED_ADDRESS_MAP.put(ActiveCampaignConstant.FIELD_FIRSTNAME, FormConstant.PORTAL_INTAKE_QUESTION_FIRST_NAME);
        FIELD_UNSUPPORTED_ADDRESS_MAP.put(ActiveCampaignConstant.FIELD_LASTNAME, FormConstant.PORTAL_INTAKE_QUESTION_LAST_NAME);
        FIELD_UNSUPPORTED_ADDRESS_MAP.put(ActiveCampaignConstant.FIELD_EMAIL, FormConstant.PORTAL_INTAKE_QUESTION_EMAIL_ADDRESS);
        FIELD_UNSUPPORTED_ADDRESS_MAP.put(ActiveCampaignConstant.FIELD_PHONE, FormConstant.PORTAL_INTAKE_QUESTION_PHONE_NUMBER);
    }

    public static Map<String, String> FIELD_BUSINESS_OWNER_REGISTER_MAP = new HashMap<>();
    static {
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_BUSINESS_INDUSTRY_V2, FormConstant.QUESTION_BUSINESS_INDUSTRY_INFORMATION);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_BUSINESS_OPEN_DATE_V2, FormConstant.QUESTION_BUSINESS_OPENING_DATE);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_NUMBER_OF_EMPLOYEES_V2, FormConstant.QUESTION_BUSINESS_EMPLOYEE_COUNT);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_GENDER_IDENTITY_V2, FormConstant.PORTAL_INTAKE_QUESTION_GENDER);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_PREFERRED_LANGUAGE_V2, FormConstant.PORTAL_INTAKE_QUESTION_LANGUAGE_SPEAKING);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_ETHNICITY_V2, FormConstant.PORTAL_INTAKE_QUESTION_ETHNICITY);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_MILITARY_AFFILIATION_V2, FormConstant.PORTAL_INTAKE_QUESTION_MILITARY_AFFILIATION);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_LGBTQIA_IDENTIFICATION_V2, FormConstant.PORTAL_INTAKE_QUESTION_LGBTQIA);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_1ST_PRIORITY_AREA_V2, FormConstant.PORTAL_INTAKE_QUESTION_FIRST_PRIORITY_AREA);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_2ND_PRIORITY_AREA_V2, FormConstant.PORTAL_INTAKE_QUESTION_SECOND_PRIORITY_AREA);
        FIELD_BUSINESS_OWNER_REGISTER_MAP.put(ActiveCampaignConstant.FIELD_SELECTED_RESOURCES_V2, FormConstant.PORTAL_INTAKE_QUESTION_RESOURCE_INTERESTED);
    }

}
