/**
 * ***************************************************
 * * Description :
 * * File        : PortalHelper
 * * Author      : Hung Tran
 * * Date        : Nov 12, 2024
 * ***************************************************
 **/
package com.formos.huub.helper.portal;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.constant.FormConstant;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.portals.*;
import com.formos.huub.domain.response.portals.*;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.mapper.location.LocationMapper;
import com.formos.huub.mapper.portalfeature.PortalFeatureMapper;
import com.formos.huub.mapper.portals.PortalHostMapper;
import com.formos.huub.mapper.portals.PortalStateMapper;
import com.formos.huub.mapper.program.ProgramMapper;
import com.formos.huub.mapper.program.ProgramTermMapper;
import com.formos.huub.mapper.program.ProgramTermVendorMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.service.invite.InviteService;
import com.formos.huub.validator.PortalValidator;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import tech.jhipster.security.RandomUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortalHelper {

    PortalRepository portalRepository;
    CalendarEventRepository calendarEventRepository;

    ProgramRepository programRepository;

    ProgramTermRepository programTermRepository;

    ProgramTermVendorRepository programTermVendorRepository;

    ProgramMapper programMapper;

    ProgramTermMapper programTermMapper;

    ProgramTermVendorMapper programTermVendorMapper;

    FeatureRepository featureRepository;

    PortalFeatureRepository portalFeatureRepository;

    PortalFeatureMapper portalFeatureMapper;

    LocationRepository locationRepository;

    PortalStateRepository portalStateRepository;

    PortalStateMapper portalStateMapper;

    LocationMapper locationMapper;

    UserRepository userRepository;

    PortalHostRepository portalHostRepository;

    InviteService inviteService;

    PortalHostMapper portalHostMapper;

    PortalValidator portalValidator;

    UserAnswerFormRepository userAnswerFormRepository;

    /**
     * get Portal For Invite Portal Host
     *
     * @param request          RequestInvitePortalHost
     * @param invitePortalHost PortalHost
     * @return Portal object
     */
    public String getPortalNameForInvitePortalHost(RequestInvitePortalHost request, PortalHost invitePortalHost) {
        if (Objects.isNull(invitePortalHost.getPortal())) {
            return request.getPortalName();
        } else {
            return invitePortalHost.getPortal().getPlatformName();
        }
    }

    /**
     * send Invitation Email
     *
     * @param portalName Portal
     */
    public void sendInvitationEmail(String email, String firstName, String lastName, String portalName, Boolean isExist, String inviteToken, Portal portal) {
        var fullName = String.format("%s %s", firstName, lastName);
        inviteService.sendInvitationPortalHost(email, fullName, inviteToken, portalName, isExist, false, portal);
    }

    /**
     * Get All portal state by portal id
     *
     * @param portal portal
     * @return list response ResponsePortalState
     */
    public ResponsePortalLocation getStateOfPortal(Portal portal) {
        var country = locationRepository
            .findAllByGeoNameIdAndLocationType(portal.getCountryId(), LocationTypeEnum.COUNTRY)
            .orElse(new Location());
        var response = ResponsePortalLocation.builder().country(locationMapper.toResponse(country)).build();
        var portalStates = portalStateRepository.getAllByPortalId(portal.getId());
        if (ObjectUtils.isEmpty(portalStates)) {
            return response;
        }
        var gson = new Gson();
        Type listType = new TypeToken<List<ResponseLocation>>() {
        }.getType();
        var states = portalStates
            .stream()
            .map(
                ele ->
                    ResponsePortalState.builder()
                        .id(UUID.fromString(ele.getId()))
                        .countryCode(ele.getCountryCode())
                        .countryName(ele.getCountryName())
                        .state(gson.fromJson(ele.getState(), ResponseLocation.class))
                        .cities(gson.fromJson(ele.getCities(), listType))
                        .zipcodes(gson.fromJson(ele.getZipcodes(), listType))
                        .build()
            )
            .toList();
        response.setStates(states);
        return response;
    }

    /**
     * Save Portal Feature Setting
     *
     * @param portal  Portal
     * @param request RequestBasePortal
     */
    public void savePortalAssociations(Portal portal, RequestBasePortal request) {
        savePortalProgram(portal, request.getPortalProgramRequest());
        savePortalLocation(portal, request.getPortalLocation());
        savePortalHosts(portal, request.getPortalHosts());
    }

    /**
     * Get a portal by ID
     *
     * @param id UUID
     * @return Portal
     */
    public Portal getPortal(UUID id) {
        return portalRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
    }

    public Boolean isEnablePortalFeature(UUID portalId, FeatureCodeEnum featureCode) {
        var portalFeature = portalFeatureRepository.findById_Feature_FeatureCodeAndId_Portal_Id(featureCode, portalId);
        if (portalFeature.isPresent()) {
            return portalFeature.get().getIsActive();
        }
        return false;
    }

    /**
     * init Navigation Feature
     *
     * @param portal         Portal
     * @param featureUpdates List<RequestPortalFeature>
     */
    public void initNavigationFeature(Portal portal, List<RequestPortalFeature> featureUpdates) {
        var featureMap = new HashMap<UUID, RequestPortalFeature>();
        if (!ObjectUtils.isEmpty(featureUpdates)) {
            featureMap = (HashMap<UUID, RequestPortalFeature>) featureUpdates
                .stream()
                .collect(Collectors.toMap(RequestPortalFeature::getId, Function.identity(), (existing, replacement) -> existing));
        }
        HashMap<UUID, RequestPortalFeature> finalFeatureMap = featureMap;
        var featureInit = List.of(
            FeatureCodeEnum.PORTALS,
            FeatureCodeEnum.PORTALS_SETTING,
            FeatureCodeEnum.PORTALS_BUSINESS_OWNERS,
            FeatureCodeEnum.PORTALS_CALENDAR_AND_EVENTS,
            FeatureCodeEnum.PORTALS_REPORTS,
            FeatureCodeEnum.PORTALS_MESSAGING,
            FeatureCodeEnum.PORTALS_LEARNING_LIBRARY,
            FeatureCodeEnum.PORTALS_GRANTS,
            FeatureCodeEnum.PORTALS_COMMUNITY_PARTNERS,
            FeatureCodeEnum.PORTALS_WORK_PACKAGES,
            FeatureCodeEnum.PORTALS_INTAKE_QUESTIONS
        );
        var portalFeatures = featureRepository
            .getAllByFeatureCodeIn(featureInit)
            .stream()
            .map(ele -> buildPortalFeature(ele, portal, finalFeatureMap))
            .toList();
        portalFeatureRepository.saveAll(portalFeatures);
        if (featureRepository.existsPortalIdEnableEventCalendar(portal.getId(), FeatureCodeEnum.PORTALS_CALENDAR_AND_EVENTS)) {
            handleAddHuubEventToPortal(portal);
        }
    }

    /**
     * Save portal feature setting
     *
     * @param portal  Portals
     * @param request List<RequestPortalFeature>
     */
    public void savePortalFeatureSetting(Portal portal, List<RequestPortalFeature> request) {
        if (ObjectUtils.isEmpty(request)) {
            return;
        }
        Map<UUID, RequestPortalFeature> featureMap = request
            .stream()
            .collect(Collectors.toMap(RequestPortalFeature::getId, Function.identity(), (existing, replacement) -> existing));
        List<UUID> featureIds = new ArrayList<>(featureMap.keySet());
        List<PortalFeature> portalFeatures = portalFeatureRepository.findAllByPortalIdAndFeatureIds(portal.getId(), featureIds);
        portalFeatures.forEach(portalFeature -> {
            Feature feature = portalFeature.getId().getFeature();
            RequestPortalFeature featureUpdate = featureMap.get(feature.getId());
            if (Objects.nonNull(featureUpdate)) {
                if (
                    !FeatureCodeEnum.PORTALS_CALENDAR_AND_EVENTS.equals(feature.getFeatureCode())
                    || portalFeature.getIsActive().equals(featureUpdate.getIsActive())
                ) {
                    portalFeature.setIsActive(featureUpdate.getIsActive());
                    return;
                }
                if (featureUpdate.getIsActive()) {
                    handleAddHuubEventToPortal(portal);
                } else {
                    handleRemoveHuubEventToPortal(portal);
                }
            }
        });
        portalFeatureRepository.saveAll(portalFeatures);
    }

    private void handleAddHuubEventToPortal(Portal portal) {
        var calendarEvents = calendarEventRepository.findAllHuubEvent();
        calendarEvents.forEach(ce -> ce.getPortals().add(portal));
        calendarEventRepository.saveAll(calendarEvents);
    }

    private void handleRemoveHuubEventToPortal(Portal portal) {
        var calendarEvents = calendarEventRepository.findAllHuubEvent();
        calendarEvents.forEach(ce -> ce.getPortals().remove(portal));
        calendarEventRepository.saveAll(calendarEvents);
    }

    /**
     * get Invite Portal Host is exist or create new.
     *
     * @param request RequestInvitePortalHost
     * @return PortalHost Object
     */
    public PortalHost getOrCreateInvitePortalHost(RequestInvitePortalHost request) {
        if (Objects.isNull(request.getPortalHostId())) {
            return getOrCreateInvitePortalHostByEmail(request);
        }
        var portalHost = getPortalHost(request.getPortalHostId());
        portalHostMapper.partialEntityInvite(portalHost, request);
        return portalHost;
    }

    /**
     * save Portal Program
     *
     * @param portal  Portals
     * @param request RequestPortalProgram
     */
    private void savePortalProgram(Portal portal, RequestPortalProgram request) {
        if (Objects.isNull(request)) {
            portal.setProgram(null);
            return;
        }
        portalValidator.validateContractDates(request);
        Program program = initializeProgram(request, portal);
        program = programRepository.saveAndFlush(program);
        saveDataProgramTerm(request.getProgramTermRequests(), program);
        saveAboutScreenConfiguration(portal, program);
    }


    private Program initializeProgram(RequestPortalProgram request, Portal portal) {
        Program program;
        if (Objects.nonNull(request.getId())) {
            program = programRepository.findById(request.getId()).orElse(new Program());
            programMapper.partialEntity(program, request);
        } else {
            program = programMapper.toEntity(request);
        }
        program.setPortal(portal);
        return program;
    }

    /**
     * save Portal Location
     *
     * @param portal                Portal Object
     * @param requestPortalLocation List<RequestPortalState>
     */
    private void savePortalLocation(Portal portal, RequestPortalLocation requestPortalLocation) {
        Set<PortalState> currentStates = portal.getStates();
        saveLocationCountry(requestPortalLocation);
        if (ObjectUtils.isEmpty(requestPortalLocation) || ObjectUtils.isEmpty(requestPortalLocation.getStates())) {
            portalStateRepository.deleteAllByPortalId(portal.getId());
            return;
        }

        Map<UUID, PortalState> currentStepMap = createCurrentStateMap(currentStates);
        List<PortalState> stateToDelete = new ArrayList<>(currentStates);
        List<PortalState> stateToSave = new ArrayList<>();
        List<Location> newLocations = new ArrayList<>();
        for (RequestPortalState request : requestPortalLocation.getStates()) {
            PortalState portalState = updateOrCreateState(request, currentStepMap);
            portalState.setCountryCode(request.getCountryCode());
            if (Objects.nonNull(portalState.getId())) {
                stateToDelete.remove(portalState);
            }
            portalState.setPortal(portal);
            stateToSave.add(portalState);
            newLocations.addAll(saveLocation(request));
        }
        locationRepository.saveAll(newLocations);
        portalStateRepository.saveAll(stateToSave);
        portalStateRepository.deleteAll(stateToDelete);
    }

    /**
     * @param portal             Portal
     * @param requestPortalHosts List<RequestPortalHost>
     */
    private void savePortalHosts(Portal portal, List<RequestPortalHost> requestPortalHosts) {
        var invitePortalHosts = invitePortalHostWhenCreate(requestPortalHosts);
        portalValidator.validatePortalHost(requestPortalHosts);
        if (ObjectUtils.isEmpty(requestPortalHosts)) {
            List<PortalHost> portalHosts = portalHostRepository.getAllByPortalId(portal.getId());
            breakAssociationPortalAndPortalHost(portalHosts);
            return;
        }
        var portalHostIds = requestPortalHosts.stream().map(ele -> getPortalHostId(invitePortalHosts,
                ele.getEmail(), ele.getId()))
            .filter(Objects::nonNull).toList();
        Set<PortalHost> currentHosts = portalHostRepository.getAllByPortalIdOrIdIn(portal.getId(), portalHostIds);
        Map<UUID, PortalHost> currentStepMap = createCurrentPortalHostMap(currentHosts);
        List<PortalHost> hostToDelete = new ArrayList<>(currentHosts);
        List<PortalHost> hostToSave = new ArrayList<>();
        for (RequestPortalHost request : requestPortalHosts) {
            var portalHostId = getPortalHostId(invitePortalHosts, request.getEmail(), request.getId());
            request.setId(portalHostId);
            PortalHost portalState = updateOrCreatePortalHost(request, currentStepMap);
            if (Objects.nonNull(portalState.getId())) {
                hostToDelete.remove(portalState);
            }
            portalState.setPortal(portal);
            hostToSave.add(portalState);
        }
        savePortalHostAndUser(hostToSave);
        breakAssociationPortalAndPortalHost(hostToDelete);
        sendMultiInvitePortalHost(invitePortalHosts, portal);
    }

    private void savePortalHostAndUser(List<PortalHost> hostsToSave) {
        // Filter active hosts with non-null user IDs
        List<PortalHost> activePortalHosts = hostsToSave.stream()
            .filter(host -> Objects.nonNull(host.getUserId()))
            .toList();

        // Map of userId to PortalHost for easy lookup
        Map<UUID, PortalHost> portalHostMap = ObjectUtils.convertToMap(activePortalHosts, PortalHost::getUserId);

        // Retrieve and update corresponding users
        List<UUID> userPortalHostIds = activePortalHosts.stream()
            .map(PortalHost::getUserId)
            .toList();
        List<User> updatedUsers = userRepository.findAllById(userPortalHostIds).stream()
            .peek(user -> updateUserFromPortalHost(user, portalHostMap.get(user.getId())))
            .toList();

        // Save updated users and portal hosts
        userRepository.saveAll(updatedUsers);
        portalHostRepository.saveAll(hostsToSave);
    }

    private void updateUserFromPortalHost(User user, PortalHost portalHost) {
        if (portalHost != null) {
            user.setFirstName(portalHost.getFirstName());
            user.setLastName(portalHost.getLastName());
            user.makeNormalizedFullName();
        }
    }

    private UUID getPortalHostId(List<ResponsePortalHostInvite> invitePortalHosts, String email, UUID id) {
        return invitePortalHosts.stream().filter(invite -> invite.getEmail().equals(email))
            .map(ResponsePortalHostInvite::getId)
            .findFirst()
            .orElse(id);
    }

    public ResponseExistPortalHost existPortalHost(String email) {
        ResponseExistPortalHost response = new ResponseExistPortalHost();
        response.setIsExist(false);

        if (email == null || email.isBlank()) {
            return response;
        }

        userRepository.findOneByEmail(email).ifPresentOrElse(user -> {
            boolean isPortalHost = user.getAuthorities().stream()
                .map(Authority::getName)
                .anyMatch(AuthoritiesConstants.PORTAL_HOST::equals);

            if (!isPortalHost) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0041));
            }

            populateResponseWithUserDetails(response, user);

            portalHostRepository.findByEmailIgnoreCase(email).ifPresent(portalHost -> {
                if (portalHost.getPortal() != null) {
                    response.setPortalName(portalHost.getPortal().getPlatformName());
                }
            });
        }, () -> response.setIsExist(false));

        return response;
    }

    public List<ResponsePortalHostInvite> invitePortalHostWhenCreate(List<RequestPortalHost> requests) {
        if (ObjectUtils.isEmpty(requests)) {
            return List.of();
        }
        return requests.stream().filter(ele -> Objects.isNull(ele.getId())).map(request -> {
            var existPortalHost = existPortalHost(request.getEmail());
            var requestInvite = new RequestInvitePortalHost();
            BeanUtils.copyProperties(request, requestInvite);
            PortalHost invitePortalHost = getOrCreateInvitePortalHost(requestInvite);

            if (Boolean.TRUE.equals(existPortalHost.getIsExist()) && Objects.nonNull(existPortalHost.getPortalName())) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0039, existPortalHost.getPortalName()));
            }
            String inviteToken = RandomUtil.generateResetKey();
            if (Boolean.TRUE.equals(existPortalHost.getIsExist())) {
                invitePortalHost.setUserId(existPortalHost.getUserId());
                invitePortalHost.setStatus(PortalHostStatusEnum.ACTIVE);
            } else {
                invitePortalHost.setStatus(PortalHostStatusEnum.INVITED);
            }
            invitePortalHost.setInviteToken(inviteToken);
            invitePortalHost.setInviteExpire(Instant.now().plus(BusinessConstant.NUMBER_90, ChronoUnit.DAYS));
            invitePortalHost = portalHostRepository.save(invitePortalHost);
            return ResponsePortalHostInvite.builder()
                .id(invitePortalHost.getId())
                .firstName(invitePortalHost.getFirstName())
                .lastName(invitePortalHost.getLastName())
                .isExist(existPortalHost.getIsExist())
                .inviteToken(inviteToken)
                .email(invitePortalHost.getEmail())
                .build();
        }).toList();
    }

    private void sendMultiInvitePortalHost(List<ResponsePortalHostInvite> responsePortalHostInvites, Portal portal) {
        responsePortalHostInvites.forEach(ele -> sendInvitationEmail(ele.getEmail(), ele.getFirstName(),
            ele.getLastName(), portal.getPlatformName(), ele.getIsExist(), ele.getInviteToken(), portal));
    }

    private void populateResponseWithUserDetails(ResponseExistPortalHost response, User user) {
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setIsExist(true);
        response.setEmail(user.getEmail());
        response.setUserId(user.getId());
        response.setStatus(user.getStatus());
    }

    /**
     * Break association Portal and Portal Host
     *
     * @param portalHosts List<PortalHost>
     */
    private void breakAssociationPortalAndPortalHost(List<PortalHost> portalHosts) {
        portalHosts.forEach(portalHost -> portalHost.setPortal(null));
        portalHostRepository.saveAll(portalHosts);
    }

    /**
     * save Location Country
     *
     * @param request RequestPortalLocation
     */
    private void saveLocationCountry(RequestPortalLocation request) {
        if (ObjectUtils.isEmpty(request) || Objects.isNull(request.getCountry())) {
            return;
        }
        var countryRequest = request.getCountry();
        var locations = locationRepository.findAllByGeoNameIdIn(List.of(countryRequest.getGeoNameId()));
        if (!ObjectUtils.isEmpty(locations)) {
            return;
        }
        var country = locationMapper.toEntity(countryRequest, LocationTypeEnum.COUNTRY);
        locationRepository.save(country);
    }

    /**
     * Create a map of current states with their IDs as keys.
     *
     * @param currentStates Set of current states.
     * @return Map of current states with IDs as keys.
     */
    private Map<UUID, PortalState> createCurrentStateMap(Set<PortalState> currentStates) {
        if (ObjectUtils.isEmpty(currentStates)) {
            return new HashMap<>();
        }
        return currentStates.stream().collect(Collectors.toMap(PortalState::getId, Function.identity()));
    }

    /**
     * Create a map of current portal hosts with their IDs as keys.
     *
     * @param currentHosts Set of current portal hosts.
     * @return Map of current portal hosts with IDs as keys.
     */
    private Map<UUID, PortalHost> createCurrentPortalHostMap(Set<PortalHost> currentHosts) {
        if (ObjectUtils.isEmpty(currentHosts)) {
            return new HashMap<>();
        }
        return currentHosts.stream().collect(Collectors.toMap(PortalHost::getId, Function.identity()));
    }

    /**
     * Get an existing state by ID or create a new one if it doesn't exist.
     *
     * @param request         RequestPortalState object.
     * @param currentStateMap Map of current states.
     * @return PortalState object.
     */
    private PortalState updateOrCreateState(RequestPortalState request, Map<UUID, PortalState> currentStateMap) {
        var stateId = request.getState().getGeoNameId();
        var cities = request.getCities().stream().map(RequestLocation::getGeoNameId).collect(Collectors.joining(","));
        var zipcodes = request.getZipcodes().stream().map(RequestLocation::getGeoNameId).collect(Collectors.joining(","));
        var priorityOrder = request.getPriorityOrder();
        if (Objects.nonNull(request.getId())) {
            var stepUpdate = currentStateMap.get(request.getId());
            portalStateMapper.partialEntity(stepUpdate, stateId, cities, zipcodes, priorityOrder);
            return stepUpdate;
        }
        return portalStateMapper.toEntity(stateId, cities, zipcodes, priorityOrder);
    }

    /**
     * Get an existing portal host by ID or create a new one if it doesn't exist.
     *
     * @param request        RequestPortalHost object.
     * @param currentHostMap Map of current states.
     * @return PortalHost object.
     */
    private PortalHost updateOrCreatePortalHost(RequestPortalHost request, Map<UUID, PortalHost> currentHostMap) {
        if (Objects.nonNull(request.getId())) {
            var hostUpdate = currentHostMap.get(request.getId());
            portalHostMapper.partialEntity(hostUpdate, request);
            return hostUpdate;
        }
        return portalHostMapper.toEntity(request);
    }

    /**
     * save Location
     *
     * @param request RequestPortalLocation
     */
    private List<Location> saveLocation(RequestPortalState request) {
        List<String> geoNameIds = collectGeoNameIds(request);

        Map<String, Location> locationExistMaps = locationRepository
            .findAllByGeoNameIdIn(geoNameIds)
            .stream()
            .collect(Collectors.toMap(Location::getGeoNameId, Function.identity(), (existing, replacement) -> existing));

        return collectNewLocations(request, locationExistMaps);
    }

    /**
     * collect GeoNameIds
     *
     * @param request RequestPortalLocation
     * @return List<String>
     */
    private List<String> collectGeoNameIds(RequestPortalState request) {
        List<String> geoNameIds = new ArrayList<>();
        geoNameIds.add(request.getState().getGeoNameId());
        geoNameIds.addAll(request.getCities().stream().map(RequestLocation::getGeoNameId).toList());
        geoNameIds.addAll(request.getZipcodes().stream().map(RequestLocation::getGeoNameId).toList());
        return geoNameIds;
    }

    /**
     * collect New Locations
     *
     * @param request           RequestPortalLocation
     * @param locationExistMaps Map<String, Location>
     * @return List<Location>
     */
    private List<Location> collectNewLocations(RequestPortalState request, Map<String, Location> locationExistMaps) {
        List<Location> newLocations = new ArrayList<>();

        if (!locationExistMaps.containsKey(request.getState().getGeoNameId())) {
            newLocations.add(locationMapper.toEntity(request.getState(), LocationTypeEnum.STATE));
        }

        newLocations.addAll(
            request
                .getCities()
                .stream()
                .filter(ele -> !locationExistMaps.containsKey(ele.getGeoNameId()))
                .map(ele -> locationMapper.toEntity(ele, LocationTypeEnum.CITY))
                .toList()
        );

        newLocations.addAll(
            request
                .getZipcodes()
                .stream()
                .filter(ele -> !locationExistMaps.containsKey(ele.getGeoNameId()))
                .map(ele -> locationMapper.toEntity(ele, LocationTypeEnum.ZIPCODE))
                .toList()
        );

        return newLocations;
    }

    /**
     * save Data Program Term
     *
     * @param programTermRequests List<RequestProgramTerm>
     * @param program             Program
     */
    private void saveDataProgramTerm(List<RequestProgramTerm> programTermRequests, Program program) {
        // Initialize current program terms and map by ID
        Set<ProgramTerm> currentProgramTerms = program.getProgramTerms();
        Map<UUID, ProgramTerm> currentProgramTermMap = currentProgramTerms != null
            ? currentProgramTerms.stream().collect(Collectors.toMap(ProgramTerm::getId, Function.identity()))
            : new HashMap<>();
        assert currentProgramTerms != null;
        List<ProgramTerm> termsToDelete = ObjectUtils.isEmpty(currentProgramTerms) ? new ArrayList<>() : new ArrayList<>(currentProgramTerms);

        // If no program term requests, delete all current terms and return
        if (ObjectUtils.isEmpty(programTermRequests)) {
            programTermRepository.deleteAll(termsToDelete);
            return;
        }
        if (hasOverlappingDates(programTermRequests)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0070));
        }
        Map<UUID, User> programManagerMap = getPortalManagerByIds(programTermRequests);
        List<ProgramTerm> termsToSave = new ArrayList<>();
        boolean isCurrentTermAlreadySet = false;
        Instant currentDate = Instant.now();

        for (RequestProgramTerm request : programTermRequests) {
            validateProgramTermDates(request, isCurrentTermAlreadySet, currentDate);

            ProgramTerm programTerm = getOrCreateProgramTerm(request, currentProgramTermMap, program);
            updateProgramTermDetails(programTerm, request, program, programManagerMap, currentDate);
            isCurrentTermAlreadySet = isCurrentTermAlreadySet || programTerm.getIsCurrent();

            termsToDelete.remove(programTerm);
            termsToSave.add(programTerm);
        }

        programTermRepository.saveAll(termsToSave);
        programTermRepository.deleteAll(termsToDelete);
    }

    private void validateProgramTermDates(RequestProgramTerm request, boolean isCurrentTermAlreadySet, Instant currentDate) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Program Term Start"));
        }
        if (request.getStartDate().isBefore(currentDate) && request.getEndDate().isAfter(currentDate) && isCurrentTermAlreadySet) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0070));
        }
    }

    private ProgramTerm getOrCreateProgramTerm(RequestProgramTerm request, Map<UUID, ProgramTerm> currentProgramTermMap, Program program) {
        if (request.getId() != null) {
            ProgramTerm existingProgramTerm = currentProgramTermMap.get(request.getId());
            programTermMapper.partialEntity(existingProgramTerm, request);
            return existingProgramTerm;
        }
        ProgramTerm newProgramTerm = programTermMapper.toEntity(request);
        newProgramTerm.setProgram(program);
        return newProgramTerm;
    }

    public static boolean hasOverlappingDates(List<RequestProgramTerm> objects) {
        objects.sort(Comparator.comparing(RequestProgramTerm::getStartDate));
        for (int i = 1; i < objects.size(); i++) {
            RequestProgramTerm previous = objects.get(i - 1);
            RequestProgramTerm current = objects.get(i);

            if (!previous.getEndDate().isBefore(current.getStartDate())) {
                return true;
            }
        }
        return false;
    }

    private void updateProgramTermDetails(ProgramTerm programTerm, RequestProgramTerm request, Program program,
                                          Map<UUID, User> programManagerMap, Instant currentDate) {
        boolean isCurrent = request.getStartDate().isBefore(currentDate) && request.getEndDate().isAfter(currentDate);
        StatusEnum status = currentDate.isBefore(request.getEndDate()) ? StatusEnum.ACTIVE : StatusEnum.ENDED;

        programTerm.setIsCurrent(isCurrent);
        programTerm.setStatus(status);
        programTerm.setProgramManager(programManagerMap.get(request.getProgramManagerId()));
        programTerm.setProgram(program);
        Set<ProgramTermVendor> vendors = new HashSet<>(saveDataProgramTermVendor(request.getVendorRequests(), programTerm));
        programTerm.setProgramTermVendors(vendors);
    }

    private Map<UUID, User> getPortalManagerByIds(List<RequestProgramTerm> programTermRequests) {
        var programManagerIds = programTermRequests.stream().map(RequestProgramTerm::getProgramManagerId).toList();
        var users = userRepository.findAllById(programManagerIds);
        return ObjectUtils.convertToMap(users, User::getId);
    }

    /**
     * save Data Program TermVendor
     *
     * @param programTermVendorRequest List<RequestProgramTermVendor>
     * @param programTerm              ProgramTerm
     * @return List<ProgramTermVendor>
     */
    private List<ProgramTermVendor> saveDataProgramTermVendor(
        List<RequestProgramTermVendor> programTermVendorRequest,
        ProgramTerm programTerm
    ) {
        if (ObjectUtils.isEmpty(programTermVendorRequest)) {
            programTermVendorRequest = Collections.emptyList();
        }
        Set<ProgramTermVendor> currentProgramTermVendors = ObjectUtils.isEmpty(programTerm.getProgramTermVendors())
            ? Collections.emptySet()
            : new HashSet<>(programTerm.getProgramTermVendors());

        Map<UUID, ProgramTermVendor> existingVendorsMap = currentProgramTermVendors.stream()
            .collect(Collectors.toMap(ProgramTermVendor::getId, Function.identity()));

        List<ProgramTermVendor> vendorsToSave = new ArrayList<>();
        Set<UUID> updatedVendorIds = new HashSet<>();

        for (RequestProgramTermVendor request : programTermVendorRequest) {
            var calculatedHours = calculatorCalculatedHours(request.getVendorBudget(), request.getContractedRate());

            if (Objects.nonNull(request.getId()) && existingVendorsMap.containsKey(request.getId())) {
                ProgramTermVendor programTermVendor = existingVendorsMap.get(request.getId());
                programTermVendorMapper.partialEntity(programTermVendor, request);
                programTermVendor.setCalculatedHours(calculatedHours);
                programTermVendor.setAllocatedHours(Objects.nonNull(programTermVendor.getAllocatedHours()) ? programTermVendor.getAllocatedHours() : 0);
                vendorsToSave.add(programTermVendor);
                updatedVendorIds.add(request.getId());
            } else {
                ProgramTermVendor programTermVendor = programTermVendorMapper.toEntity(request);
                programTermVendor.setProgramTerm(programTerm);
                programTermVendor.setAllocatedHours(Objects.nonNull(programTermVendor.getAllocatedHours()) ? programTermVendor.getAllocatedHours() : 0);
                programTermVendor.setCalculatedHours(calculatedHours);
                vendorsToSave.add(programTermVendor);
            }
        }
        List<ProgramTermVendor> vendorsToDelete = currentProgramTermVendors.stream()
            .filter(vendor -> !updatedVendorIds.contains(vendor.getId()))
            .toList();

        programTermVendorRepository.deleteAll(vendorsToDelete);
        return programTermVendorRepository.saveAll(vendorsToSave);
    }


    private Float calculatorCalculatedHours(BigDecimal vendorBudget, BigDecimal contractedRate) {
        if (Objects.isNull(vendorBudget) || Objects.isNull(contractedRate) || contractedRate.compareTo(BigDecimal.ZERO) == 0) {
            return BusinessConstant.NUMBER_0.floatValue();
        }
        BigDecimal result = vendorBudget.divide(contractedRate, 2, RoundingMode.FLOOR);
        BigDecimal roundedResult = result.setScale(0, RoundingMode.FLOOR);
        return roundedResult.floatValue();
    }

    /**
     * build Portal Feature
     *
     * @param feature         Feature
     * @param portal          Portal
     * @param finalFeatureMap HashMap<UUID, RequestPortalFeature>
     * @return PortalFeature
     */
    private PortalFeature buildPortalFeature(Feature feature, Portal portal, HashMap<UUID, RequestPortalFeature> finalFeatureMap) {
        var active = !feature.getIsDynamic();
        var featureUpdate = finalFeatureMap.get(feature.getId());
        if (Objects.nonNull(featureUpdate)) {
            active = featureUpdate.getIsActive();
        }
        return portalFeatureMapper.toObjectEntity(portal, feature, active);
    }

    /**
     * Get Portal host by ID
     *
     * @param portalHostId UUID
     * @return PortalHost
     */
    private PortalHost getPortalHost(UUID portalHostId) {
        return portalHostRepository
            .findByIdAndStatus(portalHostId, PortalHostStatusEnum.INVITED)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal Host")));
    }

    /**
     * get Or Create Invite Portal Host By Email
     *
     * @param request RequestInvitePortalHost
     * @return PortalHost
     */
    private PortalHost getOrCreateInvitePortalHostByEmail(RequestInvitePortalHost request) {
        var portalHostOpt = portalHostRepository.findByEmailIgnoreCase(request.getEmail());
        if (portalHostOpt.isPresent()) {
            var portalHost = portalHostOpt.get();
            var portal = portalHost.getPortal();
            portalHost.setFirstName(request.getFirstName());
            portalHost.setLastName(request.getLastName());
            if (Objects.nonNull(portal) && PortalHostStatusEnum.INVITED.equals(portalHost.getStatus())) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0040, portal.getPlatformName()));
            }
            return portalHost;
        }
        return portalHostMapper.toEntityInvite(request, PortalHostStatusEnum.INVITED);
    }

    private void saveAboutScreenConfiguration(Portal portal, Program program) {
        if (hasExistingAboutScreenConfiguration(portal) || Objects.isNull(portal.getPrimaryContactName())
            || Objects.isNull(portal.getPrimaryContactEmail()) || Objects.isNull(portal.getPrimaryContactPhone())
            || Objects.isNull(portal.getCity())) {
            return;
        }
        Map<String, String> replacements = Map.of(
            FormConstant.PARAM_PORTAL_NAME, portal.getPlatformName(),
            FormConstant.PARAM_PROGRAM_NAME, program.getName(),
            FormConstant.PARAM_PORTAL_PRIMARY_CONTACT_NAME, portal.getPrimaryContactName(),
            FormConstant.PARAM_PORTAL_PRIMARY_CONTACT_EMAIL, portal.getPrimaryContactEmail(),
            FormConstant.PARAM_PORTAL_PRIMARY_CONTACT_PHONE, portal.getPrimaryContactPhone(),
            FormConstant.PARAM_PORTAL_CITY, portal.getCity()
        );
        var defaultAnswers = userAnswerFormRepository
            .getAllByPortalIdAndFormDefaultData(FormCodeEnum.PORTAL_ABOUT_SCREEN_CONFIGURATION)
            .stream()
            .map(ele -> createUserAnswerForm(ele, portal.getId(), replacements))
            .toList();
        userAnswerFormRepository.saveAll(defaultAnswers);
    }

    private boolean hasExistingAboutScreenConfiguration(Portal portal) {
        var currentData = userAnswerFormRepository
            .getAllByEntryIdAndFormCode(portal.getId(), EntryTypeEnum.PORTAL, FormCodeEnum.PORTAL_ABOUT_SCREEN_CONFIGURATION);
        return !ObjectUtils.isEmpty(currentData);
    }

    private UserAnswerForm createUserAnswerForm(UserAnswerForm ele, UUID portalId, Map<String, String> replacements) {
        var userAnswerForm = new UserAnswerForm();
        userAnswerForm.setQuestionId(ele.getQuestionId());
        userAnswerForm.setIsActive(true);
        userAnswerForm.setEntryId(portalId);
        userAnswerForm.setEntryType(EntryTypeEnum.PORTAL);
        userAnswerForm.setAdditionalAnswer(replaceValueForAnswer(ele.getAdditionalAnswer(), replacements));
        return userAnswerForm;
    }

    private String replaceValueForAnswer(String answer, Map<String, String> replacements) {
        if (ObjectUtils.isEmpty(answer)) {
            return answer;
        }
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (answer.contains(entry.getKey())) {
                answer = answer.replace(entry.getKey(), entry.getValue());
            }
        }
        return answer;
    }
}
