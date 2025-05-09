package com.formos.huub.service.communitypartner;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.RequestSearchCommunityPartner;
import com.formos.huub.domain.request.calendarevent.RequestCalendarEventLink;
import com.formos.huub.domain.request.calendarevent.RequestPortalCalendarEventSetting;
import com.formos.huub.domain.request.communitypartner.*;
import com.formos.huub.domain.request.invitemember.RequestInviteMember;
import com.formos.huub.domain.request.usersetting.RequestSearchAllCommunityPartner;
import com.formos.huub.domain.response.communitypartner.*;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.communitypartner.CommunityPartnerMapper;
import com.formos.huub.mapper.technicaladvisor.TechnicalAdvisorMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.calendarevent.CalendarEventService;
import com.formos.huub.service.invite.InviteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityPartnerService {

    CommunityPartnerRepository communityPartnerRepository;

    CommunityPartnerMapper communityPartnerMapper;

    TechnicalAdvisorMapper technicalAdvisorMapper;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    PortalRepository portalRepository;

    CalendarEventService calendarEventService;

    UserRepository userRepository;

    CalendarIntegrationRepository calendarIntegrationRepository;

    InviteService inviteService;

    /**
     * Get all community partner
     *
     * @return List<ResponseCommunityPartner>
     */
    public List<ResponseCommunityPartner> getAllCommunityPartners(RequestSearchAllCommunityPartner request) {
        List<CommunityPartner> result = communityPartnerRepository.getAllByCondition(request).stream().toList();
        return communityPartnerMapper.toListResponse(result);
    }

    /**
     * search Community Partners
     *
     * @param request RequestSearchCommunityPartner
     * @return Map<String, Object>
     */
    public Map<String, Object> searchCommunityPartners(RequestSearchCommunityPartner request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "cp.name,asc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var conditions = request.getSearchConditions()
            .stream().peek(condition -> {
                if (condition.getColumn().equals("isVendor")){
                    condition.setColumn(("coalesce(isVendor, false)"));
                }
            }).toList();
        request.setSearchConditions(conditions);
        var data = communityPartnerRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * get Detail Community Partner
     *
     * @param communityPartnerId UUID
     * @return ResponseDetailCommunityPartner
     */
    public ResponseDetailCommunityPartner getDetailCommunityPartner(UUID communityPartnerId) {
        var communityPartner = getCommunityPartner(communityPartnerId);
        var about = communityPartnerMapper.toResponseAbout(communityPartner);
        about.setServiceTypes(convertServiceTypes(communityPartner.getServiceTypes()));
        about.setIsActive(StatusEnum.ACTIVE.equals(communityPartner.getStatus()));
        var portalIds = communityPartner.getPortals().stream().map(Portal::getId).toList();
        about.setPortals(portalIds);
        var eventSettings = ResponseEventSettings.builder()
            .eventbriteUrl(communityPartner.getEventbriteUrl())
            .urlICal(communityPartner.getICalUrl())
            .build();

        // get technical advisor
        var responseTechnicalAdvisors = getResponseTechnicalAdvisors(communityPartnerId);

        List<IResponseCommunityPartnerStaff> communityPartnerStaffs = userRepository.getCommunityPartnerStaffs(communityPartnerId);

        return ResponseDetailCommunityPartner.builder()
            .about(about)
            .technicalAdvisors(responseTechnicalAdvisors)
            .eventSettings(eventSettings)
            .communityPartnerStaffs(communityPartnerStaffs)
            .build();
    }

    private List<ResponseTechnicalAdvisor> getResponseTechnicalAdvisors(UUID communityPartnerId) {
        var technicalAdvisors = technicalAdvisorRepository.getTechnicalAdvisorByCommunityPartnerId(communityPartnerId);
        return technicalAdvisors.stream()
            .map(ta -> {
                ResponseTechnicalAdvisor response = technicalAdvisorMapper.toResponseTechnicalAdvisor(ta);
                response.setPortalIds(ta.getPortals().stream()
                    .map(Portal::getId)
                    .toList());
                return response;
            })
            .toList();
    }

    /**
     * Create profile and user for community partner
     *
     * @param request RequestCreateCommunityPartner object
     */
    public void createCommunityPartner(RequestCreateCommunityPartner request) {
        var about = request.getAbout();
        var communityPartnerStaffs = request.getCommunityPartnerStaffs();

        validateCommunityPartnerStaffs(communityPartnerStaffs, about.getIsVendor(), about.getIsActive());
        validateCommunityPartnerStatus(about, communityPartnerStaffs);

        var communityPartner = communityPartnerMapper.toEntity(about);
        communityPartner.setServiceTypes(about.toServiceTypes());
        var eventSettings = request.getEventSettings();
        if (Objects.nonNull(request.getEventSettings())) {
            communityPartner.setEventbriteUrl(eventSettings.getEventbriteUrl());
            communityPartner.setICalUrl(eventSettings.getUrlICal());
        }
        var portals = getListPortals(about.getPortals());
        communityPartner.setPortals(portals);
        communityPartner.setStatus(about.getIsActive() ? StatusEnum.ACTIVE : StatusEnum.INACTIVE);
        // Save the community partner
        communityPartner = communityPartnerRepository.save(communityPartner);

        // Save technical advisors if the community partner is a vendor
        saveTechnicalAdvisorForNewCommunityPartner(request.getTechnicalAdvisors(), communityPartner, request.getAbout().getIsVendor());

        // Add event to portal calendar
        addEventsToPortalCalendar(request.getEventSettings(), request.getAbout().getPortals(), communityPartner.getId());

        saveStaffsForNewCommunityPartner(request.getCommunityPartnerStaffs(), communityPartner);
    }

    /**
     * update Community Partner by id
     *
     * @param communityPartnerId UUID
     * @param request            RequestUpdateCommunityPartner
     */
    public void updateCommunityPartner(UUID communityPartnerId, RequestUpdateCommunityPartner request) {
        var communityPartner = getCommunityPartner(communityPartnerId);
        var about = request.getAbout();
        var communityPartnerStaffs = request.getCommunityPartnerStaffs();

        validateEmailChange(communityPartner.getEmail(), about.getEmail());
        validateCommunityPartnerStaffs(communityPartnerStaffs, about.getIsVendor(), about.getIsActive());
        validateCommunityPartnerStatus(about, communityPartnerStaffs);

        communityPartnerMapper.partialEntity(communityPartner, about);
        var eventSettings = request.getEventSettings();
        if (Objects.nonNull(request.getEventSettings())) {
            communityPartner.setEventbriteUrl(eventSettings.getEventbriteUrl());
            communityPartner.setICalUrl(eventSettings.getUrlICal());
        }
        communityPartner.setServiceTypes(about.toServiceTypes());
        var portals = getListPortals(about.getPortals());
        communityPartner.setPortals(portals);
        communityPartner.setStatus(about.getIsActive() ? StatusEnum.ACTIVE : StatusEnum.INACTIVE);
        // Save the community partner
        communityPartner = communityPartnerRepository.save(communityPartner);

        // Save technical advisors if the community partner is a vendor
        saveTechnicalAdvisors(request.getTechnicalAdvisors(), communityPartner, request.getAbout().getIsVendor());

        // Add event to portal calendar
        addEventsToPortalCalendar(request.getEventSettings(), request.getAbout().getPortals(), communityPartnerId);

        saveCommunityPartnerStaffs(communityPartnerStaffs, communityPartner);
    }

    /**
     * Validate Community Partner Status
     *
     * @param about RequestCommunityPartnerAbout
     * @param communityPartnerStaffs List<RequestCommunityPartnerStaff>
     */
    private void validateCommunityPartnerStatus(RequestCommunityPartnerAbout about, List<RequestCommunityPartnerStaff> communityPartnerStaffs) {
        if (Boolean.FALSE.equals(about.getIsActive())) {
            return;
        }

        boolean hasActivePrimaryStaff = communityPartnerStaffs.stream()
            .anyMatch(staff -> Boolean.TRUE.equals(staff.getIsPrimary()) && UserStatusEnum.ACTIVE.equals(staff.getStatus()));

        if (!hasActivePrimaryStaff) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0087));
        }
    }


    /**
     * Add Events To PortalCalendar
     *
     * @param eventSettings RequestEventSettings
     * @param portalIds List<UUID>
     * @param communityPartnerId UUID
     */
    private void addEventsToPortalCalendar(RequestEventSettings eventSettings, List<UUID> portalIds, UUID communityPartnerId) {
        for (UUID portalId : portalIds) {
            List<RequestCalendarEventLink> calendarEventLinks = generateCalendarEventLinks(eventSettings, communityPartnerId, portalId);

            validateEventSettings(calendarEventLinks, portalId);

            saveEventSettingsToPortal(calendarEventLinks, portalId, communityPartnerId);
        }
    }

    /**
     * Generate CalendarEvent Links
     *
     * @param eventSettings RequestEventSettings
     * @param communityPartnerId UUID
     * @param portalId UUID
     * @return List<RequestCalendarEventLink>
     */
    private List<RequestCalendarEventLink> generateCalendarEventLinks(RequestEventSettings eventSettings, UUID communityPartnerId, UUID portalId) {
        return Stream.of(
                createEventLink(communityPartnerId, eventSettings.getEventbriteUrl(), CalendarTypeEnum.EVENTBRITE, portalId),
                createEventLink(communityPartnerId, eventSettings.getUrlICal(), CalendarTypeEnum.ICALENDAR, portalId)
            )
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }

    /**
     * Validate EventSettings
     *
     * @param calendarEventLinks List<RequestCalendarEventLink>
     * @param portalId UUID
     */
    private void validateEventSettings(List<RequestCalendarEventLink> calendarEventLinks, UUID portalId) {
        if (ObjectUtils.isEmpty(calendarEventLinks)) {
            return;
        }
        for (RequestCalendarEventLink eventLink : calendarEventLinks) {
            boolean isDuplicate = calendarIntegrationRepository.findByUrlAndPortalId(eventLink.getUrl(), portalId)
                .filter(calendar -> !calendar.getId().equals(eventLink.getId()))
                .isPresent();
            if (isDuplicate) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0048, eventLink.getCalendarType().getName()));
            }
        }
    }

    /**
     * Save EventSettings To Portal
     *
     * @param calendarEventLinks List<RequestCalendarEventLink>
     * @param portalId UUID
     * @param communityPartnerId UUID
     */
    private void saveEventSettingsToPortal(List<RequestCalendarEventLink> calendarEventLinks, UUID portalId, UUID communityPartnerId) {
        RequestPortalCalendarEventSetting calendarEventSetting = new RequestPortalCalendarEventSetting();
        calendarEventSetting.setPortalId(portalId);
        calendarEventSetting.setCalendarEventSettings(calendarEventLinks);
        calendarEventSetting.setCommunityPartnerId(communityPartnerId);
        calendarEventService.saveCommunityPartnerEventsToPortal(calendarEventSetting);
    }


    /**
     * Create Event Link
     *
     * @param url          String
     * @param calendarType CalendarTypeEnum
     * @return Optional<RequestCalendarEventLink>
     */
    private Optional<RequestCalendarEventLink> createEventLink(UUID communityPartnerId, String url, CalendarTypeEnum calendarType, UUID currentPortalId) {
        String calendarId = String.join("|", currentPortalId.toString(), communityPartnerId.toString(), IntegrateByEnum.COMMUNITY_PARTNER.getValue(), url);
        UUID id = calendarIntegrationRepository.findByCalendarId(calendarId).map(CalendarIntegration::getId).orElse(null);
        return (url == null || url.isEmpty())
            ? Optional.empty()
            : Optional.of(new RequestCalendarEventLink(id, url, calendarType));
    }

    /**
     * get List Portals
     *
     * @param portalIds List<UUID>
     * @return Set<Portal>
     */
    private Set<Portal> getListPortals(List<UUID> portalIds) {
        if (ObjectUtils.isEmpty(portalIds)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Portals"));
        }
        var portals = portalRepository.findAllById(portalIds);
        if (portals.size() != portalIds.size()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portals"));
        }
        return new HashSet<>(portals);
    }


    private void validateEmailChange(String oldEmail, String newEmail) {
        var emailExist = communityPartnerRepository.existsByEmailIgnoreCase(newEmail);
        if (!ObjectUtils.isEmpty(oldEmail) && !oldEmail.equalsIgnoreCase(newEmail) && emailExist) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }
    }

    /**
     * Saves or removes technical advisors for community partner
     *
     * @param technicalAdvisorRequests List<RequestVendorTechnicalAdvisor>
     * @param communityPartner         CommunityPartner
     * @param isVendor                 Boolean
     */
    private void saveTechnicalAdvisors(List<RequestVendorTechnicalAdvisor> technicalAdvisorRequests, CommunityPartner communityPartner, Boolean isVendor) {
        if (Boolean.FALSE.equals(isVendor)) {
            return;
        }
        var currentTechnicalAdvisors = technicalAdvisorRepository.getAllByCommunityPartnerId(communityPartner.getId());
        if (ObjectUtils.isEmpty(technicalAdvisorRequests)) {
            removeObsoleteTechnicalAdvisors(currentTechnicalAdvisors, List.of());
            return;
        }
        var technicalAdvisorRequestMap = ObjectUtils.convertToMap(technicalAdvisorRequests, RequestVendorTechnicalAdvisor::getId);
        var technicalAdvisorIds = technicalAdvisorRequests.stream()
            .map(RequestVendorTechnicalAdvisor::getId)
            .toList();

        var technicalAdvisors = technicalAdvisorRepository.findAllById(technicalAdvisorIds).stream()
            .peek(technicalAdvisor -> updateTechnicalAdvisor(technicalAdvisor, communityPartner, technicalAdvisorRequestMap.get(technicalAdvisor.getId())))
            .toList();
        // Save updated technical advisors
        technicalAdvisorRepository.saveAll(technicalAdvisors);

        // Handle removal of obsolete technical advisors
        removeObsoleteTechnicalAdvisors(currentTechnicalAdvisors, technicalAdvisorIds);
    }

    /**
     * Update technical advisor details from request.
     *
     * @param advisor          TechnicalAdvisor
     * @param communityPartner CommunityPartner
     * @param request          RequestVendorTechnicalAdvisor
     */
    private void updateTechnicalAdvisor(TechnicalAdvisor advisor, CommunityPartner communityPartner, RequestVendorTechnicalAdvisor request) {
        advisor.setCommunityPartner(communityPartner);
        var user = advisor.getUser();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.makeNormalizedFullName();

        if (UserStatusEnum.INACTIVE.equals(request.getStatus())) {
            user.setStatus(UserStatusEnum.INACTIVE);
            user.setActivated(false);
        }

        if (ObjectUtils.isEmpty(request.getPortalIds())) {
            var portalsCommunityPartner = communityPartner.getPortals();
            var portals = ObjectUtils.isEmpty(advisor.getPortals()) ? new HashSet<>(portalsCommunityPartner) : advisor.getPortals();

            portals.addAll(
                portalsCommunityPartner.stream()
                    .filter(ele -> !portals.contains(ele))
                    .toList()
            );

            advisor.setPortals(portals);
        } else {
            advisor.setPortals(getListPortals(request.getPortalIds()));
        }

        advisor.setUser(user);
    }

    /**
     * removes technical advisors not present in the updated list
     *
     * @param currentTechnicalAdvisors List<TechnicalAdvisor>
     * @param updatedTechnicalAdvisors List<UUID>
     */
    private void removeObsoleteTechnicalAdvisors(List<TechnicalAdvisor> currentTechnicalAdvisors, List<UUID> updatedTechnicalAdvisors) {
        var removedAdvisors = currentTechnicalAdvisors.stream()
            .filter(ele -> !updatedTechnicalAdvisors.contains(ele.getId()))
            .map(this::updateInactiveTechnicalAdvisor)
            .toList();
        technicalAdvisorRepository.saveAll(removedAdvisors);
    }

    private TechnicalAdvisor updateInactiveTechnicalAdvisor(TechnicalAdvisor technicalAdvisor) {
        var user = technicalAdvisor.getUser();
        if (UserStatusEnum.ACTIVE.equals(user.getStatus())) {
            user.setStatus(UserStatusEnum.INACTIVE);
        }
        technicalAdvisor.setUser(user);
        return technicalAdvisor;
    }

    /**
     * Save CommunityPartner Staffs
     *
     * @param communityPartnerStaffs List<RequestCommunityPartnerStaff>
     * @param communityPartner       CommunityPartner
     */
    private void saveCommunityPartnerStaffs(List<RequestCommunityPartnerStaff> communityPartnerStaffs, CommunityPartner communityPartner) {
        List<User> currentCommunityPartnerStaffs = userRepository.getAllCommunityPartnerStaffByCommunityPartnerId(communityPartner.getId());
        if (ObjectUtils.isEmpty(communityPartnerStaffs)) {
            handleStaffAssociateWithCommunityPartner(currentCommunityPartnerStaffs, List.of());
            return;
        }
        var staffRequestMap = ObjectUtils.convertToMap(communityPartnerStaffs, RequestCommunityPartnerStaff::getId);
        var staffIds = communityPartnerStaffs.stream()
            .map(RequestCommunityPartnerStaff::getId)
            .toList();

        List<User> staffs = userRepository.findAllById(staffIds).stream()
            .peek(staff -> updateCommunityPartnerStaff(staff, communityPartner, staffRequestMap.get(staff.getId())))
            .toList();

        userRepository.saveAll(staffs);

        handleStaffAssociateWithCommunityPartner(currentCommunityPartnerStaffs, staffIds);
    }

    /**
     * Save Staffs For New CommunityPartner
     *
     * @param communityPartnerStaffs List<RequestCommunityPartnerStaff>
     * @param communityPartner CommunityPartner
     */
    private void saveStaffsForNewCommunityPartner(List<RequestCommunityPartnerStaff> communityPartnerStaffs, CommunityPartner communityPartner) {
        communityPartnerStaffs.forEach(staff -> {
            RequestInviteMember inviteMember = RequestInviteMember.builder()
                .email(staff.getEmail())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .isNavigator(staff.getIsNavigator())
                .isPrimary(staff.getIsPrimary())
                .build();
            inviteService.inviteCommunityPartnerStaff(inviteMember, communityPartner.getId());
        });
    }

    /**
     * Validate Community Partner Staffs
     *
     * @param communityPartnerStaffs List<RequestCommunityPartnerStaff>
     * @param isVendor Boolean
     * @param isActive Boolean
     */
    private void validateCommunityPartnerStaffs(List<RequestCommunityPartnerStaff> communityPartnerStaffs, Boolean isVendor, Boolean isActive) {
        if (Boolean.TRUE.equals(isActive)) {
            validateExactlyOneStaffWithRole(communityPartnerStaffs,
                RequestCommunityPartnerStaff::getIsPrimary,
                Message.Keys.E0085);
        }

        if (Boolean.TRUE.equals(isVendor)) {
            validateExactlyOneStaffWithRole(communityPartnerStaffs,
                RequestCommunityPartnerStaff::getIsNavigator,
                Message.Keys.E0086);
        }
    }

    /**
     * Validate Exactly One Staff With Role
     *
     * @param staffList List<RequestCommunityPartnerStaff>
     * @param roleExtractor Function<RequestCommunityPartnerStaff, Boolean>
     * @param errorMessageKey Message.Keys
     */
    private void validateExactlyOneStaffWithRole(List<RequestCommunityPartnerStaff> staffList,
                                                 Function<RequestCommunityPartnerStaff, Boolean> roleExtractor,
                                                 Message.Keys errorMessageKey) {
        long count = staffList.stream()
            .filter(staff -> Boolean.TRUE.equals(roleExtractor.apply(staff)))
            .count();

        if (count != 1) {
            throw new BadRequestException(MessageHelper.getMessage(errorMessageKey));
        }
    }


    /**
     * Save TechnicalAdvisor For New CommunityPartner
     *
     * @param technicalAdvisors List<RequestVendorTechnicalAdvisor>
     * @param communityPartner CommunityPartner
     * @param isVendor Boolean
     */
    private void saveTechnicalAdvisorForNewCommunityPartner(List<RequestVendorTechnicalAdvisor> technicalAdvisors, CommunityPartner communityPartner, Boolean isVendor) {
        if (Boolean.FALSE.equals(isVendor)) {
            return;
        }
        technicalAdvisors.forEach(staff -> {
            RequestInviteMember inviteMember = RequestInviteMember.builder()
                .email(staff.getEmail())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .communityPartnerId(communityPartner.getId())
                .portalIds(staff.getPortalIds())
                .build();
            inviteService.inviteTechnicalAdvisor(inviteMember);
        });
    }

    /**
     * Get Exists TechnicalAdvisor
     *
     * @param email String
     * @return ResponseExistsTechnicalAdvisor
     */
    public ResponseExistsTechnicalAdvisor getExistsTechnicalAdvisor(String email) {
        var response = ResponseExistsTechnicalAdvisor.builder().isExist(false).build();
        if (Objects.isNull(email)) {
            return response;
        }
        return userRepository.findOneByEmail(email)
            .map(user -> {
                validateTechnicalAdvisorRole(user);
                return buildResponse(user);
            })
            .orElse(response);
    }

    /**
     * Validate TechnicalAdvisor Role
     *
     * @param user User
     */
    private void validateTechnicalAdvisorRole(User user) {
        boolean isRoleTechnicalAdvisor = user.getAuthorities()
            .stream()
            .map(Authority::getName)
            .anyMatch(AuthoritiesConstants.TECHNICAL_ADVISOR::equals);

        if (!isRoleTechnicalAdvisor) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0055));
        }
    }

    /**
     *
     *
     * @param user User
     * @return ResponseExistsTechnicalAdvisor
     */
    private ResponseExistsTechnicalAdvisor buildResponse(User user) {
        ResponseExistsTechnicalAdvisor response =
            ResponseExistsTechnicalAdvisor.builder().isExist(true).build();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    /**
     * Handle Staff Associate With CommunityPartner
     *
     * @param currentCommunityPartnerStaffs List<User>
     * @param updatedStaffIds               List<UUID>
     */
    private void handleStaffAssociateWithCommunityPartner(List<User> currentCommunityPartnerStaffs, List<UUID> updatedStaffIds) {
        List<User> removedStaffs = currentCommunityPartnerStaffs.stream()
            .filter(ele -> !updatedStaffIds.contains(ele.getId()))
            .map(this::breakAssociateWithCommunityPartner)
            .toList();
        userRepository.saveAll(removedStaffs);
    }

    /**
     * Break Associate With CommunityPartner
     *
     * @param user User
     * @return User
     */
    private User breakAssociateWithCommunityPartner(User user) {
        user.setCommunityPartner(null);
        return user;
    }

    /**
     * Update CommunityPartner Staff
     *
     * @param staff            User
     * @param communityPartner CommunityPartner
     * @param request          RequestCommunityPartnerStaff
     */
    private void updateCommunityPartnerStaff(User staff, CommunityPartner communityPartner, RequestCommunityPartnerStaff request) {
        staff.setCommunityPartner(communityPartner);
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setIsNavigator(request.getIsNavigator());
        staff.setIsPrimary(request.getIsPrimary());
        staff.makeNormalizedFullName();
    }


    /**
     * Deletes a community partner if no technical advisor are linked.
     *
     * @param id UUID
     */
    public void deleteCommunityPartner(UUID id) {
        CommunityPartner communityPartner = getCommunityPartner(id);
        if (StatusEnum.ACTIVE.equals(communityPartner.getStatus())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0090));
        }

        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            communityPartnerRepository.deleteById(id);
            List<User> staffs = userRepository.getAllCommunityPartnerStaffByCommunityPartnerId(id).stream()
                .map(this::removeStaffsFromCommunityPartner)
                .toList();
            userRepository.saveAll(staffs);

            var portalIds = communityPartner.getPortals().stream().map(Portal::getId).toList();
            removeTechnicalAdvisorFromCommunityPartner(id, portalIds);
        } else {
            UUID portalId = PortalContextHolder.getPortalId();
            communityPartnerRepository.deletePortalCommunityPartner(portalId, id);
            removeTechnicalAdvisorFromCommunityPartner(id, Collections.singletonList(portalId));
        }
    }

    /**
     * Remove TechnicalAdvisor From CommunityPartner
     *
     * @param communityPartnerId UUID
     */
    @Transactional
    public void removeTechnicalAdvisorFromCommunityPartner(UUID communityPartnerId, List<UUID> portalIds) {
        List<TechnicalAdvisor> technicalAdvisors = technicalAdvisorRepository.getTechnicalAdvisorByCommunityPartnerId(communityPartnerId).stream()
            .map(this::breakTechnicalAdvisorAssociateWithCommunityPartner)
            .toList();
        technicalAdvisorRepository.saveAll(technicalAdvisors);

        // Break Technical Advisor with Portal
        List<UUID> technicalAdvisorIds = technicalAdvisors.stream().map(TechnicalAdvisor::getId).toList();
        technicalAdvisorRepository.deleteTechnicalAdvisorPortal(technicalAdvisorIds, portalIds);
    }

    /**
     * Break TechnicalAdvisor Associate With CommunityPartner
     *
     * @param technicalAdvisor TechnicalAdvisor
     * @return TechnicalAdvisor
     */
    private TechnicalAdvisor breakTechnicalAdvisorAssociateWithCommunityPartner(TechnicalAdvisor technicalAdvisor) {
        technicalAdvisor.setCommunityPartner(null);
        return technicalAdvisor;
    }

    /**
     * Remove Staffs From CommunityPartner
     *
     * @param user User
     * @return User
     */
    private User removeStaffsFromCommunityPartner(User user) {
        user.setCommunityPartner(null);
        user.setIsNavigator(false);
        user.setIsPrimary(false);
        return user;
    }

    private List<String> convertServiceTypes(String serviceTypes) {
        if (ObjectUtils.isEmpty(serviceTypes)) {
            return List.of();
        }
        return Arrays.stream(serviceTypes.split(",")).toList();
    }

    /**
     * Fetches a community partner by ID or throws an exception if not found.
     *
     * @param id UUID
     * @return CommunityPartner
     */
    private CommunityPartner getCommunityPartner(UUID id) {
        return communityPartnerRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Community Partner")));
    }

    public List<IResponseCommunityPartner> getAllCommunityPartnerMeta(UUID portalId) {
        if (Objects.isNull(portalId)){
            portalId =  PortalContextHolder.getPortalId();
        }
        return communityPartnerRepository.getAllCommunityPartnerMeta(portalId);
    }

    public String getCommunityPartnerNavigator(String communityPartnerId, String userId) {
        UUID communityPartnerUUID = UUID.fromString(communityPartnerId);

        return userRepository.findCommunityPartnerNavigatorById(communityPartnerUUID)
            .filter(user -> isValidUser(user, userId))
            .map(User::getNormalizedFullName)
            .orElse(null);
    }

    public List<IResponseCommunityPartner> getAllCommunityPartnerByPortalId(String portalId) {
        return communityPartnerRepository.getAllCommunityPartnerByPortalId(UUID.fromString(portalId));
    }

    private boolean isValidUser(User user, String userId) {
        return Objects.isNull(userId) || userId.isEmpty() || !user.getId().equals(UUID.fromString(userId));
    }

    public String getCommunityPartnerPrimary(String communityPartnerId, String userId) {
        UUID communityPartnerUUID = UUID.fromString(communityPartnerId);

        return userRepository.getCommunityPartnerPrimaryById(communityPartnerUUID)
            .filter(user -> isValidUser(user, userId))
            .map(User::getNormalizedFullName)
            .orElse(null);
    }

    /**
     * Handle Activate Community Partner
     *
     * @param user User
     * @param communityPartner CommunityPartner
     */
    public void handleActivateCommunityPartner(User user, CommunityPartner communityPartner) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.COMMUNITY_PARTNER)
            || Boolean.TRUE.equals(user.getFirstLoggedIn())) {
            return;
        }

        if (Boolean.TRUE.equals(user.getIsPrimary())) {
            communityPartner.setStatus(StatusEnum.ACTIVE);
            communityPartnerRepository.save(communityPartner);
        }
    }

}
