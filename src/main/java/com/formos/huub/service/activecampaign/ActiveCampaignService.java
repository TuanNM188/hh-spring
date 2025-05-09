package com.formos.huub.service.activecampaign;

import com.formos.huub.domain.request.activecampaign.*;
import com.formos.huub.domain.response.activecampaign.*;
import com.formos.huub.framework.exception.ActiveCampaignException;
import com.formos.huub.framework.properties.ActiveCampaignProperties;
import com.formos.huub.service.resttemplate.RestTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ActiveCampaignService {

    private static final String API_TOKEN_HEADER = "Api-Token";
    private static final String CONTACTS_ENDPOINT = "/contacts";
    private static final String FIELDS_ENDPOINT = "/fields";
    private static final String TAGS_ENDPOINT = "/tags";
    private static final String CONTACT_TAGS_ENDPOINT = "/contactTags";
    private static final String FIELD_VALUES_ENDPOINT = "/fieldValues";
    private static final int DEFAULT_LIMIT = 100;

    private final ActiveCampaignProperties activeCampaignProperties;
    private final RestTemplateService restTemplateService;

    private HttpHeaders headers;

    /**
     * Initialize headers for Active Campaign API
     *
     * @return HttpHeaders
     */
    public HttpHeaders getHeaders() {
        if (Objects.isNull(headers)) {
            headers = new HttpHeaders();
            headers.set(API_TOKEN_HEADER, activeCampaignProperties.getAccessKey());
            headers.setContentType(MediaType.APPLICATION_JSON);

            log.debug("Initialized Active Campaign headers");
        }
        return headers;
    }

    /**
     * Get all custom fields
     *
     * @return ResponseListField
     */
    public ResponseListField getListAllCustomField() {
        try {
            String url = buildUrl(FIELDS_ENDPOINT + "?limit=" + DEFAULT_LIMIT);
            ResponseEntity<ResponseListField> response = restTemplateService.sendGetRequest(
                url,
                getHeaders(),
                ResponseListField.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to get custom fields. Status: {}", response.getStatusCode());
                return null;
            }

            log.debug("Successfully retrieved {} custom fields",
                Optional.ofNullable(response.getBody())
                    .map(fields -> fields.getFields().size())
                    .orElse(0));

            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting custom fields", e);
            throw new ActiveCampaignException("Failed to get custom fields", e);
        }
    }

    /**
     * Sync data contact to Active Campaign
     *
     * @param contact RequestContacts
     */
    public void syncDataContact(RequestContacts contact) {
        try {
            String url = buildUrl("/contact/sync");
            RequestCreateContact request = new RequestCreateContact(contact);

            ResponseEntity<ResponseContacts> response = restTemplateService.sendPostRequest(
                url,
                request,
                getHeaders(),
                ResponseContacts.class
            );

            logContactOperation("sync", contact.getEmail(), response.getStatusCode());
            response.getBody();

        } catch (Exception e) {
            log.error("Error syncing contact: {}", contact.getEmail(), e);
            throw new ActiveCampaignException("Failed to sync contact", e);
        }
    }

    /**
     * Create contact to Active Campaign
     *
     * @param contact RequestContacts
     * @return ResponseContacts
     */
    public ResponseContacts createContact(RequestContacts contact) {
        try {
            String url = buildUrl(CONTACTS_ENDPOINT);
            RequestCreateContact request = new RequestCreateContact(contact);

            ResponseEntity<ResponseContacts> response = restTemplateService.sendPostRequest(
                url,
                request,
                getHeaders(),
                ResponseContacts.class
            );

            logContactOperation("create", contact.getEmail(), response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error creating contact: {}", contact.getEmail(), e);
            throw new ActiveCampaignException("Failed to create contact", e);
        }
    }

    /**
     * Get contact by email
     *
     * @param email String
     * @return ResponseContacts
     */
    public ResponseContacts getContactCampaignByEmail(String email) {
        try {
            String baseEmail = email.split("\\+")[0];
            String url = buildUrl(CONTACTS_ENDPOINT + "?email_like=" + baseEmail);

            ResponseEntity<ResponseContactsFilters> response = restTemplateService.sendGetRequest(
                url,
                getHeaders(),
                ResponseContactsFilters.class
            );

            ResponseContactsFilters responseFilter = response.getBody();
            if (Objects.isNull(responseFilter) || responseFilter.getContacts().isEmpty()) {
                log.debug("No contact found for email: {}", email);
                return null;
            }

            var matchingContact = responseFilter.getContacts().stream()
                .filter(contact -> contact.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

            if (Objects.isNull(matchingContact)) {
                log.debug("No exact email match found for: {}", email);
                return null;
            }

            return ResponseContacts
                .builder()
                .contact(matchingContact)
                .build();

        } catch (Exception e) {
            log.error("Error getting contact by email: {}", email, e);
            throw new ActiveCampaignException("Failed to get contact by email", e);
        }
    }

    /**
     * Get tagId by tagName
     *
     * @param tagName String
     * @return String
     */
    public String getTagId(String tagName) {
        try {
            String url = buildUrl(TAGS_ENDPOINT);

            ResponseEntity<ResponseTags> response = restTemplateService.sendGetRequest(
                url,
                getHeaders(),
                ResponseTags.class
            );

            ResponseTags responseTags = response.getBody();

            if (Objects.isNull(responseTags) || responseTags.getTags().isEmpty()) {
                log.debug("No tag found for tagName: {}", tagName);
                return null;
            }

            String tagId = responseTags.getTags().stream()
                .filter(tag -> tag.getTag().equalsIgnoreCase(tagName))
                .map(ResponseTags.Tag::getId)
                .findFirst()
                .orElse(null);

            if (Objects.isNull(tagId)) {
                RequestCreateTag.RequestTag requestTag = RequestCreateTag.RequestTag.builder()
                    .tag(tagName)
                    .tagType("contact")
                    .build();
                RequestCreateTag requestCreateTag = RequestCreateTag.builder()
                    .tag(requestTag)
                    .build();

                ResponseTag tag = createTag(requestCreateTag);

                if (Objects.nonNull(tag) && Objects.nonNull(tag.getTag())) {
                    return tag.getTag().getId();
                }
            }

            log.info("Get tagId successfully. Status {}", response.getStatusCode());
            return tagId;

        } catch (Exception e) {
            log.error("Get tagId error", e);
            throw new ActiveCampaignException("Failed to get tagId", e);
        }
    }

    /**
     * Create Tag
     *
     * @param request RequestCreateTag
     * @return ResponseTag
     */
    public ResponseTag createTag(RequestCreateTag request) {
        try {
            String url = buildUrl(TAGS_ENDPOINT);

            ResponseEntity<ResponseTag> response = restTemplateService.sendPostRequest(
                url,
                request,
                getHeaders(),
                ResponseTag.class
            );

            log.info("Create tag {} successfully. Status {}", request.getTag().getTag(), response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Create tag error: {}", request.getTag().getTag(), e);
            throw new ActiveCampaignException("Failed to create tag", e);
        }
    }

    /**
     * Create ContactTags
     *
     * @param contactTags RequestContactTags
     * @return ResponseContactTags
     */
    public ResponseContactTags createContactTags(RequestContactTags contactTags) {
        try {
            String url = buildUrl(CONTACT_TAGS_ENDPOINT);
            RequestCreateContactTags request = new RequestCreateContactTags(contactTags);

            ResponseEntity<ResponseContactTags> response = restTemplateService.sendPostRequest(
                url,
                request,
                getHeaders(),
                ResponseContactTags.class
            );

            log.info("Create contact tags {} for contact {} successfully. Status {}", contactTags.getTag(), contactTags.getContact(), response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error creating contact tags: {}", contactTags.getTag(), e);
            throw new ActiveCampaignException("Failed to create contact tags", e);
        }
    }

    /**
     * Update CustomFieldValue For Contact
     *
     * @param request RequestUpdateCustomField
     * @return ResponseCustomFieldContact
     */
    public ResponseCustomFieldContact updateCustomFieldValueForContact(RequestUpdateCustomField request) {
        try {
            String url = buildUrl(String.join("/", FIELD_VALUES_ENDPOINT, request.getFieldValue().getContact()));

            ResponseEntity<ResponseCustomFieldContact> response = restTemplateService.sendPostRequest(
                url,
                request,
                getHeaders(),
                ResponseCustomFieldContact.class
            );

            log.info("Update custom field for contact {} successfully. Status {}", request.getFieldValue().getContact(), response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Update custom field error: {}", request.getFieldValue().getValue(), e);
            throw new ActiveCampaignException("Failed to update custom field", e);
        }
    }

    /**
     * Get ContactFieldValue
     *
     * @param contactId String
     * @param fieldId String
     * @return String
     */
    public String getContactFieldValue(String contactId, String fieldId) {
        try {
            String url = buildUrl(String.join("/", CONTACTS_ENDPOINT, contactId, FIELD_VALUES_ENDPOINT));

            ResponseEntity<ResponseListContactFieldValue> response = restTemplateService.sendGetRequest(
                url,
                getHeaders(),
                ResponseListContactFieldValue.class
            );

            ResponseListContactFieldValue responseFieldsValue = response.getBody();

            if (Objects.isNull(responseFieldsValue) || responseFieldsValue.getFieldValues().isEmpty()) {
                log.debug("No contact field value for fieldId: {}", fieldId);
                return null;
            }

            String fieldValue = responseFieldsValue.getFieldValues().stream()
                .filter(value -> value.getField().equalsIgnoreCase(fieldId))
                .map(ResponseListContactFieldValue.ResponseFieldValue::getValue)
                .findFirst()
                .orElse(null);


            log.info("Get contact field value successfully. Status {}", response.getStatusCode());
            return fieldValue;

        } catch (Exception e) {
            log.error("Get contact field value error", e);
            throw new ActiveCampaignException("Failed to Get contact field value", e);
        }
    }

    /**
     * Update contact by contactId
     *
     * @param contactId String
     * @param contact   RequestContacts
     * @return ResponseContacts
     */
    public ResponseContacts updateContact(String contactId, RequestContacts contact) {

        try {
            String url = buildUrl(CONTACTS_ENDPOINT + "/" + contactId);
            RequestCreateContact request = new RequestCreateContact(contact);

            ResponseEntity<ResponseContacts> response = restTemplateService.sendPutRequest(
                url,
                request,
                getHeaders(),
                ResponseContacts.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to update contact {}. Status: {}", contactId, response.getStatusCode());
                return null;
            }

            logContactOperation("update", contact.getEmail(), response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error updating contact: {}", contactId, e);
            throw new ActiveCampaignException("Failed to update contact", e);
        }

    }

    /**
     * Build URL for Active Campaign API
     *
     * @param endpoint String
     * @return String
     */
    private String buildUrl(String endpoint) {
        return activeCampaignProperties.getBaseUrl()
            .replace("{accountName}", activeCampaignProperties.getAccountName())
            .replace("{apiVersion}", activeCampaignProperties.getApiVersion())
            .concat(endpoint);
    }

    /**
     * Log contact operation
     *
     * @param operation String
     * @param email     String
     * @param status    HttpStatusCode
     */
    private void logContactOperation(String operation, String email, HttpStatusCode status) {
        log.info("{} contact operation completed for email: {}. Status: {}",
            operation.toUpperCase(),
            email,
            status);
    }
}
