/**
 * ***************************************************
 * * Description :
 * * File        : EmailTemplatePathsContants
 * * Author      : Hung Tran
 * * Date        : Jul 02, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.constant;

public final class EmailTemplatePathsConstants {

    private EmailTemplatePathsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String INVITE_TECHNICAL_ADVISOR = "mail/account/invitationEmail";

    public static final String INVITE_NEW_PORTAL_HOST = "mail/portal/invitationNewPortalHost";

    public static final String INVITE_EXIST_PORTAL_HOST = "mail/portal/invitationExistPortalHost";

    public static final String INVITE_MEMBER_PORTAL_HOST = "mail/member/invitationMemberPortalHost";

    public static final String INVITE_MEMBER_BUSINESS_OWNER = "mail/member/invitationMemberBusinessOwner";

    public static final String RECEIVE_NEW_MESSAGE = "mail/directmessage/newMessage";

    public static final String MENTION_IN_MESSAGE = "mail/directmessage/mentionMessage";

    public static final String INVITE_MEMBER_COMMUNITY_PARTNER = "mail/member/invitationCommunityPartner";

    public static final String INVITE_MEMBER_COMMUNITY_PARTNER_EXIST = "mail/member/invitationExistCommunityPartner";

    public static final String MEMBER_RECEIVE_NEW_FOLLOWER = "mail/member/memberReceiveNewFollower";

    public static final String NEW_MENTION_POST_FOR_MEMBER = "mail/member/newMentionPostForMember";
    public static final String NEW_MENTION_COMMENT_FOR_MEMBER = "mail/member/newMentionCommentForMember";

    public static final String NEW_ACTIVITY_POST_NOTIFY_ALL_BY_ADMIN = "mail/member/newActivityPostNotifyAllByAdmin";
    public static final String NEW_ACTIVITY_POST_FOLLOWER = "mail/member/newActivityPostFollower";
    public static final String NEW_REQUEST_JOIN_GROUP_BY_MEMBER = "mail/member/newRequestJoinGroupByMember";
    public static final String NEW_PROMOTE_MEMBER_IN_GROUP = "mail/member/newPromoteMemberInGroup";
    public static final String NEW_REQUEST_JOIN_GROUP_ACCEPTED = "mail/member/newRequestJoinGroupAccepted";
    public static final String NEW_REQUEST_JOIN_GROUP_REJECTED = "mail/member/newRequestJoinGroupRejected";
    public static final String NEW_NOTIFICATION_POST_IN_GROUP = "mail/member/newNotificationPostInGroup";
    public static final String NEW_UPDATE_MANAGE_IN_GROUP = "mail/member/newUpdateManageInGroup";
    public static final String NEW_INVITE_JOIN_GROUP_TO_MEMBER = "mail/member/newInviteJoinGroupToMember";

    public static final String MEMBER_RECEIVE_NEW_COMMENT_POST = "mail/member/memberReceiveNewCommentPost";

    public static final String MEMBER_RECEIVE_NEW_REPLY_COMMENT = "mail/member/memberReceiveNewReplyComment";

    public static final String RESTRICT_MEMBER_POST_COMMENT = "mail/member/restrictMemberPostComment";
    public static final String RESULT_APPOINTMENT_SURVEY_EMAIL = "mail/appointment/resultAppointmentSurveyEmail";
    public static final String MEMBER_FLAG_POST_FOR_MANAGER = "mail/member/memberFlagPostForManager";
    public static final String MEMBER_FLAG_COMMENT_FOR_MANAGER = "mail/member/memberFlagCommentForManager";

    public static final String COURSE_REGISTRATION = "mail/learninglibrary/courseRegistration";

    public static final String COURSE_REGISTRATION_APPROVAL = "mail/learninglibrary/courseRegistrationApproval";

    public static final String COURSE_REGISTRATION_DENIED = "mail/learninglibrary/courseRegistrationDenied";

    public static final String SUBMIT_SUCCESS_TECHNICAL_ASSISTANCE_APPLICATION = "mail/businessowner/submitTechnicalAssistanceBo";

    public static final String NEED_REVIEW_SUBMIT_TECHNICAL_ASSISTANCE_APPLICATION = "mail/businessowner/needReviewNewTechnicalAssistance";

    public static final String APPROVED_APPLICATION_FOR_BUSINESS_OWNER = "mail/application/approvedApplicationsForBusinessOwner";

    public static final String ASSIGN_ADVISOR_APPLICATION_FOR_BUSINESS_OWNER = "mail/application/assignAdvisorForApplicationBO";

    public static final String APPROVED_APPLICATION_FOR_COMMUNITY_PARTNER_NAVIGATOR = "mail/application/assignApplicationForVendor";

    public static final String DENIED_APPLICATION_FOR_BUSINESS_OWNER = "mail/application/deniedApplicationForBusinessOwner";

    public static final String NEW_PROJECT = "mail/project/newProject";
    public static final String EXTENSION_REQUEST_PROJECT = "mail/project/extensionRequestProject";
    public static final String APPROVE_EXTENSION_REQUEST_PROJECT = "mail/project/approveExtensionRequestProject";
    public static final String DENY_EXTENSION_REQUEST_PROJECT = "mail/project/denyExtensionRequestProject";

    public static final String APPROVE_PROJECT = "mail/project/approveProject";
    public static final String DENY_PROJECT = "mail/project/denyProject";

    public static final String PROJECT_REPORT = "mail/project/projectReport";

    public static final String PROJECT_PROPOSAL_APPROVED = "mail/project/projectProposalApprovedForBusinessOwner";
    public static final String PROJECT_PROPOSAL_DENIED = "mail/project/projectProposalDeniedForBusinessOwner";

    public static final String REMINDER_NOT_APPROVED_PROJECT = "mail/project/reminderNotApprovedProject";

    public static final String CANCEL_APPOINTMENT_EMAIL_TO_ADVISOR = "mail/appointment/cancelsAppointmentEmailToAdvisor";

    public static final String CANCEL_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER = "mail/appointment/cancelsAppointmentEmailToBusinessOwner";

    public static final String RESCHEDULE_APPOINTMENT_EMAIL_TO_ADVISOR = "mail/appointment/rescheduleAppointmentEmailToAdvisor";

    public static final String RESCHEDULE_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER =
        "mail/appointment/rescheduleAppointmentEmailToBusinessOwner";

    public static final String SCHEDULE_APPOINTMENT_EMAIL_TO_ADVISOR = "mail/appointment/scheduleAppointmentEmailToAdvisor";

    public static final String SCHEDULE_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER = "mail/appointment/scheduleAppointmentEmailToBusinessOwner";

    public static final String UPDATE_APPOINTMENT_EMAIL_TO_ADVISOR = "mail/appointment/updateAppointmentEmailToAdvisor";

    public static final String UPDATE_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER = "mail/appointment/updateAppointmentEmailToBusinessOwner";

    public static final String POST_APPOINTMENT_EMAIL_TO_BUSINESS_OWNER = "mail/appointment/24HourPostAppointmentEmailToBusinessOwner";

    public static final String POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR =
        "mail/appointment/24HourPostAppointmentReportReminderEmailToAdvisor";

    public static final String DAY_3_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR =
        "mail/appointment/3DayPostAppointmentReportReminderEmailToAdvisor";

    public static final String DAY_7_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR =
        "mail/appointment/7DayPostAppointmentReportReminderEmailToAdvisor";

    public static final String DAY_14_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR =
        "mail/appointment/14DayPostAppointmentReportReminderEmailToAdvisor";

    public static final String DAY_30_POST_APPOINTMENT_REPORT_REMINDER_EMAIL_TO_ADVISOR =
        "mail/appointment/30DayPostAppointmentReportReminderEmailToAdvisor";

    public static final String ONE_WEEK_BEFORE_ESTIMATED_COMPLETION_DATE = "mail/project-report/reminder1WeekBeforeEstimatedCompletionDate";

    public static final String ONE_WEEK_AFTER_ESTIMATED_COMPLETION_DATE = "mail/project-report/reminder1WeekAfterEstimatedCompletionDate";

    public static final String ONE_DAY_BEFORE_ESTIMATED_COMPLETION_DATE = "mail/project-report/reminder1DayBeforeEstimatedCompletionDate";

    public static final String ONE_DAY_AFTER_ESTIMATED_COMPLETION_DATE = "mail/project-report/reminder1DayAfterEstimatedCompletionDate";

    public static final String FOURTEEN_DAYS_AFTER_ESTIMATED_COMPLETION_DATE =
        "mail/project-report/reminder14DaysAfterEstimatedCompletionDate";

    public static final String THIRTY_DAYS_AFTER_ESTIMATED_COMPLETION_DATE =
        "mail/project-report/reminder30DaysAfterEstimatedCompletionDate";
    public static final String HOURS_24_PRE_APPOINTMENT_REMINDER_EMAIL_TO_BUSINESS_OWNER =
        "mail/appointment/24HoursPreAppointmentReminderEmailToBusinessOwner";
    public static final String HOURS_24_PRE_APPOINTMENT_REMINDER_EMAIL_TO_ADVISOR =
        "mail/appointment/24HoursPreAppointmentReminderEmailToAdvisor";
}
