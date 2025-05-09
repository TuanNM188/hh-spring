package com.formos.huub.service.activecampaign;

import com.formos.huub.domain.constant.ActiveCampaignConstant;
import com.formos.huub.domain.entity.Authority;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.activecampaign.RequestContactTags;
import com.formos.huub.domain.request.activecampaign.RequestContacts;
import com.formos.huub.domain.request.activecampaign.RequestUpdateCustomField;
import com.formos.huub.domain.response.activecampaign.ResponseContacts;
import com.formos.huub.domain.response.activecampaign.ResponseFields;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableAsync(proxyTargetClass = true)
@Slf4j
public class ActiveCampaignStrategy {


    ActiveCampaignService activeCampaignService;
    UserRepository userRepository;


    /**
     * Sync value with ActiveCampaign for application approval.
     *
     * @param user       User
     * @param dynamicFields Map of field names and their dynamic values
     */
    public ResponseContacts syncValueActiveCampaign(User user, Map<String, String> dynamicFields) {
        RequestContacts requestContacts = buildRequestSyncDataApplicationForContact(user, dynamicFields);

        var responseCampaign = activeCampaignService.getContactCampaignByEmail(user.getEmail());
        if (responseCampaign != null && responseCampaign.getContact() != null) {
            activeCampaignService.syncDataContact(requestContacts);
        } else {
            responseCampaign = activeCampaignService.createContact(requestContacts);
        }

        if (Objects.nonNull(user.getId()) && user.getActiveCampaignContactId() == null && responseCampaign != null && responseCampaign.getContact() != null) {
            user.setActiveCampaignContactId(responseCampaign.getContact().getId());
            userRepository.save(user);
        }

        return responseCampaign;
    }

    /**
     * Sync Value ActiveCampaign Application
     *
     * @param user User
     * @param dynamicFields Map<String, String>
     */
    @Async
    public void syncValueActiveCampaignApplication(User user, Map<String, String> dynamicFields) {
        syncValueActiveCampaign(user, dynamicFields);
    }

    /**
     * Sync Value ActiveCampaign And Add ContactTags
     *
     * @param user User
     * @param dynamicFields Map<String, String>
     * @param tag String
     */
    @Async
    public void syncValueActiveCampaignAndAddContactTags(User user, Map<String, String> dynamicFields, String tag) {
        ResponseContacts contact = syncValueActiveCampaign(user, dynamicFields);
        if (Objects.isNull(contact.getContact()) || Objects.isNull(tag)) {
            return;
        }

        String tagId = activeCampaignService.getTagId(tag);

        if (Objects.isNull(tagId)) {
            return;
        }

        RequestContactTags requestContactTags = RequestContactTags.builder()
            .contact(contact.getContact().getId())
            .tag(tagId)
            .build();

        activeCampaignService.createContactTags(requestContactTags);
    }

    /**
     * Update Custom Field Value
     *
     * @param contactId String
     * @param fieldId String
     * @param value String
     */
    @Async
    public void updateCustomFieldValue(String contactId, String fieldId, String value) {
        if (Objects.isNull(contactId) || Objects.isNull(fieldId)) {
            return;
        }

        RequestUpdateCustomField.FieldValue fieldValue = RequestUpdateCustomField.FieldValue.builder()
            .contact(contactId)
            .field(fieldId)
            .value(value)
            .build();
        RequestUpdateCustomField request = RequestUpdateCustomField.builder()
            .fieldValue(fieldValue)
            .useDefaults(false)
            .build();

        activeCampaignService.updateCustomFieldValueForContact(request);
    }

    /**
     * Handle sync BusinessOwner LastLogin
     *
     * @param user User
     */
    @Async
    public void handleBusinessOwnerLastLogin(User user) {
        boolean isBusinessOwner = user.getAuthorities()
            .stream()
            .map(Authority::getName)
            .anyMatch(AuthoritiesConstants.BUSINESS_OWNER::equals);
        if (!isBusinessOwner) {
            return;
        }
        ResponseContacts contact = syncValueActiveCampaign(user, Collections.emptyMap());
        if (Objects.isNull(contact.getContact())) {
            return;
        }
        String lastLogin = DateUtils.convertInstantToString(Instant.now());
        List<ResponseFields> customFields = activeCampaignService.getListAllCustomField().getFields();
        String fieldId = getIdCustomFields(ActiveCampaignConstant.FIELD_LAST_LOGIN_V2, customFields);
        updateCustomFieldValue(contact.getContact().getId(), fieldId, lastLogin);
    }

    /**
     * Handle Increment Contact Field
     *
     * @param user User
     * @param fieldName String
     */
    @Async
    public void handleIncrementContactField(User user, String fieldName) {
        ResponseContacts contact = syncValueActiveCampaign(user, Collections.emptyMap());
        if (Objects.isNull(contact) || Objects.isNull(contact.getContact())) {
            return;
        }
        List<ResponseFields> customFields = activeCampaignService.getListAllCustomField().getFields();
        String fieldId = getIdCustomFields(fieldName, customFields);

        if (Objects.isNull(fieldId)) {
            return;
        }

        String contactId = contact.getContact().getId();
        String currentValue  = activeCampaignService.getContactFieldValue(contactId, fieldId);
        int updatedCourseCount = 1;

        if (Objects.nonNull(currentValue) && !currentValue.isBlank()) {
            try {
                updatedCourseCount = Integer.parseInt(currentValue ) + 1;
            } catch (NumberFormatException ex) {
                log.error("Invalid count value '{}' for contact ID: {}. Defaulting to 1.", currentValue , contactId);
            }
        }
        updateCustomFieldValue(contactId, fieldId, String.valueOf(updatedCourseCount));
    }

    /**
     * Build request data for syncing with ActiveCampaign.
     *
     * @param user          User
     * @param dynamicFields Map of field names and their dynamic values
     * @return RequestContacts
     */
    public RequestContacts buildRequestSyncDataApplicationForContact(User user, Map<String, String> dynamicFields) {
        var customFields = activeCampaignService.getListAllCustomField().getFields();
        var fieldValues = new ArrayList<RequestContacts.FieldValues>();

        if (Objects.nonNull(dynamicFields) || !dynamicFields.isEmpty()) {
            dynamicFields.forEach((fieldName, fieldValue) ->
                addCustomFieldValue(fieldName, fieldValue, customFields, fieldValues)
            );
        }

        return RequestContacts.builder()
            .email(user.getEmail())
            .phone(user.getPhoneNumber())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fieldValues(fieldValues)
            .build();
    }

    /**
     * Add a custom field value to the list of field values.
     *
     * @param fieldName   Field name
     * @param fieldValue  Field value
     * @param customFields List of custom fields
     * @param fieldValues List of field values to update
     */
    private void addCustomFieldValue(String fieldName, String fieldValue,
                                     List<ResponseFields> customFields,
                                     List<RequestContacts.FieldValues> fieldValues) {
        var fieldId = getIdCustomFields(fieldName, customFields);
        if (Objects.nonNull(fieldId)) {
            fieldValues.add(new RequestContacts.FieldValues(fieldId, fieldValue));
        }
    }

    /**
     * Get the ID of a custom field by its name.
     *
     * @param fieldName Field name
     * @param fields    List of fields
     * @return Field ID or null if not found
     */
    public String getIdCustomFields(String fieldName, List<ResponseFields> fields) {
        if (Objects.isNull(fieldName) || Objects.isNull(fields) || fields.isEmpty()) {
            return null;
        }
        return fields.stream()
            .filter(ele -> ele.getPerstag().equals(fieldName))
            .findFirst()
            .map(ResponseFields::getId)
            .orElse(null);
    }
}
