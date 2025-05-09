package com.formos.huub.service.communityboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.entity.embedkey.CommunityBoardGroupSettingEmbedKey;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.communityboard.*;
import com.formos.huub.domain.request.member.RequestGetUserInPortal;
import com.formos.huub.domain.response.communityboard.*;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.mapper.communityboard.CommunityBoardGroupMapper;
import com.formos.huub.mapper.communityboard.CommunityBoardGroupSettingMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.pushnotification.PushNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.text.WordUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.formos.huub.security.SecurityUtils.*;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardGroupService extends BaseService {

    CommunityBoardGroupRepository communityBoardGroupRepository;

    CommunityBoardGroupSettingRepository communityBoardGroupSettingRepository;
    CommunityBoardGroupMemberRepository communityBoardGroupMemberRepository;

    CommunityBoardGroupMapper communityBoardGroupMapper;

    SettingDefinitionRepository settingDefinitionRepository;
    UserRepository userRepository;
    MemberRepository memberRepository;
    CommunityBoardGroupSettingMapper communityBoardGroupSettingMapper;
    PushNotificationService pushNotificationService;

    public Map<String, Object> findAllGroup(RequestGetCommunityBoardGroup request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "cbg.lastActive,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var currentUser = getCurrentUser(userRepository);
        List<String> settingValues = new ArrayList<>(Arrays.asList(
            PrivacyOptionSettingValueEnum.PUBLIC_GROUPS.getValue(),
            PrivacyOptionSettingValueEnum.PRIVATE_GROUPS.getValue()
        ));
        if (SecurityUtils.validCurrentUserIsAdmin()) {
            settingValues.add(PrivacyOptionSettingValueEnum.HIDDEN_GROUPS.getValue());
        }
        var portalId = resolvePortalId(request.getPortalId());
        var groupRoles = Arrays.asList(
            CommunityBoardGroupRoleEnum.ORGANIZER,
            CommunityBoardGroupRoleEnum.MODERATOR,
            CommunityBoardGroupRoleEnum.MEMBER
        );
        var result = communityBoardGroupRepository.findAllGroupByPortal(portalId, currentUser.getId(), settingValues, groupRoles, pageable);
        return PageUtils.toPage(result);
    }

    public Map<String, Object> findMyGroup(RequestGetCommunityBoardGroup request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "cbg.lastActive,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var currentUser = getCurrentUser(userRepository);
        var portalId = resolvePortalId(request.getPortalId());
        var groupRoles = Arrays.asList(
            CommunityBoardGroupRoleEnum.ORGANIZER,
            CommunityBoardGroupRoleEnum.MODERATOR,
            CommunityBoardGroupRoleEnum.MEMBER
        );
        var result = communityBoardGroupRepository.findMyGroupByPortal(portalId, currentUser.getId(), groupRoles, pageable);
        return PageUtils.toPage(result);
    }

    private UUID resolvePortalId(String portalId) {
        var resolvedPortalId = UUIDUtils.toUUID(portalId);
        if (Objects.isNull(resolvedPortalId)) {
            return PortalContextHolder.getPortalId();
        }
        return resolvedPortalId;
    }

    /**
     * Create CommunityBoardGroup
     *
     * @param request RequestCreateCommunityBoardGroup
     * @return UUID
     */
    public UUID createCommunityBoardGroup(RequestCreateCommunityBoardGroup request) {
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        validateGroupNameUnique(request.getDetail().getGroupName(), null);
        CommunityBoardGroup communityBoardGroup = communityBoardGroupMapper.toEntity(request);
        communityBoardGroup = communityBoardGroupRepository.save(communityBoardGroup);
        saveCommunityBoardGroupSettings(communityBoardGroup, request.getSettings());
        saveMemberCreateGroup(communityBoardGroup.getId(), request.getPortalId());
        return communityBoardGroup.getId();
    }

    /**
     * Find Detail By Id
     *
     * @param id UUID
     * @return IResponseCommunityBoardGroup
     */
    public IResponseCommunityBoardGroupDetail findDetailById(UUID id) {
        var currentUser = getCurrentUser(userRepository);

        var result = communityBoardGroupRepository.findGroupDetailById(id, currentUser.getId())
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Group")));

        if (PrivacyOptionSettingValueEnum.PUBLIC_GROUPS.getValue().equals(result.getPrivacy())) {
            return result;
        }
        if (CommunityBoardGroupStatusEnum.SEND_INVITE.equals(result.getStatus())) {
            return result;
        }
        if (CommunityBoardGroupStatusEnum.REQUEST_JOIN.equals(result.getStatus()) || Objects.isNull(result.getYourRole())) {
            throw new AccessDeniedException("Role Member");
        }
        return result;
    }

    /**
     * Find Detail By Id
     *
     * @param id UUID
     * @return ResponseCommunityBoardGroup
     */
    public ResponseCommunityBoardGroup findDetailUpdateById(UUID id) {
        CommunityBoardGroup communityBoardGroup = getCommunityBoardGroup(id);

        List<IResponseCommunityBoardGroupSetting> settings = communityBoardGroupSettingRepository
            .findAllByCommunityBoardGroupIdAndCategory(id, SettingCategoryEnum.COMMUNITY_BOARD_GROUP);

        ResponseCommunityBoardGroup response = communityBoardGroupMapper.toResponseDetail(communityBoardGroup);
        response.setSettings(communityBoardGroupSettingMapper.toListResponseFromInterface(settings));
        return response;
    }

    /**
     * Update CommunityBoardGroup
     *
     * @param id      UUID
     * @param request RequestUpdateCommunityBoardGroup
     */
    public void updateCommunityBoardGroup(UUID id, RequestUpdateCommunityBoardGroup request) {
        validateGroupNameUnique(request.getDetail().getGroupName(), id);
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        CommunityBoardGroup group = getCommunityBoardGroup(id);

        if (!communityBoardGroupMemberRepository.existsMemberInGroup(currentUser.getId(), group.getId(), List.of(CommunityBoardGroupRoleEnum.ORGANIZER))) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }

        communityBoardGroupMapper.partialUpdate(group, request);
        communityBoardGroupRepository.save(group);
        saveCommunityBoardGroupSettings(group, request.getSettings());
        var memberIds = communityBoardGroupMemberRepository.findMembersByGroupRoles(group.getId(), List.of(currentUser.getId()), List.of(CommunityBoardGroupRoleEnum.ORGANIZER));
        pushNotificationService.sendNotificationUpdateManageInGroup(group, memberIds);
    }

    public ResponseResultJoinGroup actionJoinGroup(UUID groupId) {
        var currentUser = getCurrentUser(userRepository);
        var group = getCommunityBoardGroup(groupId);

        if (communityBoardGroupMemberRepository.existsByUserIdsAndGroupId(currentUser.getId(), groupId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        var groupPrivacy = communityBoardGroupSettingRepository.findByGroupIdAndSettingKey(groupId, SettingKeyCodeEnum.PRIVACY_OPTIONS)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Group")));
        CommunityBoardGroupRoleEnum role = null;
        var status = CommunityBoardGroupStatusEnum.REQUEST_JOIN;
        if (PrivacyOptionSettingValueEnum.PUBLIC_GROUPS.getValue().equals(groupPrivacy)) {
            role = CommunityBoardGroupRoleEnum.MEMBER;
            status = CommunityBoardGroupStatusEnum.JOINED;
        }
        if (hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)) {
            role = CommunityBoardGroupRoleEnum.ORGANIZER;
            status = CommunityBoardGroupStatusEnum.JOINED;
        }
        var managerRole = List.of(CommunityBoardGroupRoleEnum.ORGANIZER);

        var groupMember = CommunityBoardGroupMember.builder()
            .groupId(groupId)
            .invitedBy(currentUser.getId())
            .status(status)
            .groupRole(role)
            .userId(currentUser.getId()).build();
        communityBoardGroupMemberRepository.save(groupMember);

        if (CommunityBoardGroupStatusEnum.REQUEST_JOIN.equals(status)) {
            communityBoardGroupMemberRepository.findMembersByGroupRoles(groupId, managerRole)
                .forEach(manager -> pushNotificationService.sendJoinGroupRequestToManager(group, manager, groupMember.getId()));
        }
        return communityBoardGroupMapper.toResponseMemberDetail(groupMember);
    }

    public void deleteCommunityBoardGroup(UUID groupId) {
        var group = getCommunityBoardGroup(groupId);

        if (!isCurrentUserAllowedToDeleteGroup(groupId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }

        communityBoardGroupRepository.delete(group);
    }

    public void acceptInviteGroup(UUID groupId) {
        // Check the current user's role
        var currentUser = getCurrentUser(userRepository);
        var groupMember = getGroupMemberByStatus(groupId, currentUser.getId(), CommunityBoardGroupStatusEnum.SEND_INVITE);
        var groupRole = hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST) ? CommunityBoardGroupRoleEnum.ORGANIZER : CommunityBoardGroupRoleEnum.MEMBER;

        // Update role to MEMBER
        groupMember.setGroupRole(groupRole);
        groupMember.setStatus(CommunityBoardGroupStatusEnum.JOINED);
        communityBoardGroupMemberRepository.save(groupMember);
    }

    public void rejectInviteGroup(UUID groupId) {
        // Check the current user's role
        var currentUser = getCurrentUser(userRepository);
        var groupMember = getGroupMemberByStatus(groupId, currentUser.getId(), CommunityBoardGroupStatusEnum.SEND_INVITE);

        // Remove member from group
        communityBoardGroupMemberRepository.delete(groupMember);
    }

    private CommunityBoardGroupMember getGroupMemberByStatus(UUID groupId, UUID userId, CommunityBoardGroupStatusEnum status) {
        var groupMember = getGroupMember(groupId, userId);

        if (!status.equals(groupMember.getStatus())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        return groupMember;
    }

    private boolean isCurrentUserAllowedToDeleteGroup(UUID groupId) {
        if (SecurityUtils.validCurrentUserIsAdmin()) {
            return true;
        }

        var currentUser = getCurrentUser(userRepository);
        return isOrganizerGroup(groupId, currentUser.getId());
    }

    private boolean isOrganizerGroup(UUID groupId, UUID currentUserId) {
        return communityBoardGroupMemberRepository.findMembersByGroupRoles(groupId, List.of(CommunityBoardGroupRoleEnum.ORGANIZER))
            .stream().anyMatch(m -> currentUserId.equals(m.getId()));
    }

    public void actionLeaveGroup(UUID groupId) {
        var currentUser = getCurrentUser(userRepository);
        var groupMemberOpt = communityBoardGroupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId());
        if (groupMemberOpt.isEmpty() || SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        var groupMember = groupMemberOpt.get();
        communityBoardGroupMemberRepository.delete(groupMember);
        if (CommunityBoardGroupRoleEnum.ORGANIZER.equals(groupMember.getGroupRole())) {
            communityBoardGroupMemberRepository.findMembersByGroupRoles(groupId, List.of(currentUser.getId()), List.of(CommunityBoardGroupRoleEnum.ORGANIZER))
                .stream().findFirst().ifPresent(userId -> communityBoardGroupMemberRepository.enableGroupCreationForMember(groupId, userId));
        }



    }

    private void saveMemberCreateGroup(UUID groupId, UUID portalId) {
        var user = getCurrentUser(userRepository);
        Set<UUID> userIds = new HashSet<>();
        userIds.add(user.getId());
        userIds.addAll(memberRepository.getAllPortalHostByPortalId(portalId));

        var members = userIds.stream()
            .map(userId -> CommunityBoardGroupMember.builder()
                .groupId(groupId)
                .groupRole(CommunityBoardGroupRoleEnum.ORGANIZER)
                .status(CommunityBoardGroupStatusEnum.JOINED)
                .userId(userId)
                .isCreateGroup(userId.equals(user.getId()))
                .build())
            .toList();
        communityBoardGroupMemberRepository.saveAll(members);
    }

    public void saveListMemberInvite(UUID groupId, RequestCommunityBoardGroupInvite invite) {
        List<UUID> userIds = UUIDUtils.toUUIDs(invite.getUserIds());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        var group = getCommunityBoardGroup(groupId);
        List<String> memberExistInGroup = communityBoardGroupMemberRepository.findAllUserByUserIdsAndGroupId(userIds, groupId);
        if (!CollectionUtils.isEmpty(memberExistInGroup)) {
            throw new BadRequestException(
                MessageHelper.getMessage(Message.Keys.E0058, WordUtils.capitalize(memberExistInGroup.stream().findFirst().get()))
            );
        }
        var currentUser = getCurrentUser(userRepository);
        var members = userIds
            .stream()
            .map(
                userId ->
                    CommunityBoardGroupMember.builder()
                        .groupId(groupId)
                        .groupRole(null)
                        .invitedBy(currentUser.getId())
                        .status(CommunityBoardGroupStatusEnum.SEND_INVITE)
                        .userId(userId)
                        .build()
            )
            .toList();
        communityBoardGroupMemberRepository.saveAll(members);
        pushNotificationService.sendInviteJoinGroupToMember(group, invite.getInviteMessage(), userIds);
    }

    private void validateCurrentUserRole(UUID groupId, List<CommunityBoardGroupRoleEnum> allowedRoles) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            return;
        }
        var currentUser = getCurrentUser(userRepository);
        var groupMember = communityBoardGroupMemberRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057)));

        if (!allowedRoles.contains(groupMember.getGroupRole())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }

    }

    public void removeMember(UUID groupId, UUID memberId) {
        // Check the current user's role
        var allowedRoles = List.of(CommunityBoardGroupRoleEnum.ORGANIZER, CommunityBoardGroupRoleEnum.MODERATOR);
        validateCurrentUserRole(groupId, allowedRoles);

        // Find member to delete
        var member = getGroupMember(groupId, memberId);
        if (memberRepository.existsUserByRole(memberId, AuthoritiesConstants.PORTAL_HOST)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0088));
        }
        if (CommunityBoardGroupRoleEnum.ORGANIZER.equals(member.getGroupRole())
            && !SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.PORTAL_HOST)
        ) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0088));
        }

        // Delete member
        communityBoardGroupMemberRepository.delete(member);

        if (Boolean.TRUE.equals(member.getIsCreateGroup())) {
            var currentUser = SecurityUtils.getCurrentUser(userRepository);
            communityBoardGroupMemberRepository.enableGroupCreationForMember(groupId, currentUser.getId());
        }
    }

    private CommunityBoardGroupMember getGroupMember(UUID groupId, UUID memberId) {
        return communityBoardGroupMemberRepository.findByGroupIdAndUserId(groupId, memberId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Member")));
    }

    public void promoteMember(UUID groupId, UUID memberId, CommunityBoardGroupRoleEnum newRole) {
        // Check the current user's role (Only ORGANIZER can promote)
        validateCurrentUserRole(groupId, List.of(CommunityBoardGroupRoleEnum.ORGANIZER));

        var member = getGroupMember(groupId, memberId);

        if (!canPromote(member.getGroupRole(), newRole)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }

        member.setGroupRole(newRole);
        communityBoardGroupMemberRepository.save(member);

        pushNotificationService.sendNotificationWhenPromoteMemberInGroup(
            getCommunityBoardGroup(groupId), memberId, newRole);
    }

    private boolean canPromote(CommunityBoardGroupRoleEnum currentRole, CommunityBoardGroupRoleEnum newRole) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.SYSTEM_ADMINISTRATOR)) {
            return true;
        }
        return switch (newRole) {
            case MODERATOR -> currentRole == CommunityBoardGroupRoleEnum.MEMBER;
            case ORGANIZER -> currentRole == CommunityBoardGroupRoleEnum.MEMBER || currentRole == CommunityBoardGroupRoleEnum.MODERATOR;
            default -> false;
        };
    }

    public void promoteModerator(UUID groupId, UUID memberId) {
        promoteMember(groupId, memberId, CommunityBoardGroupRoleEnum.MODERATOR);
    }

    public void promoteOrganizer(UUID groupId, UUID memberId) {
        promoteMember(groupId, memberId, CommunityBoardGroupRoleEnum.ORGANIZER);
    }

    public void demoteToMember(UUID groupId, UUID memberId) {
        // Check the current user's role
        var allowedRoles = List.of(CommunityBoardGroupRoleEnum.ORGANIZER);
        validateCurrentUserRole(groupId, allowedRoles);

        var member = getGroupMember(groupId, memberId);

        if (CommunityBoardGroupRoleEnum.MEMBER.equals(member.getGroupRole())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        member.setGroupRole(CommunityBoardGroupRoleEnum.MEMBER);
        communityBoardGroupMemberRepository.save(member);
    }

    /**
     * Get CommunityBoardGroup Initial Settings
     *
     * @return List<IResponseCommunityBoardGroupSetting>
     */
    public List<IResponseCommunityBoardGroupSetting> getCommunityBoardGroupInitialSettings() {
        return communityBoardGroupSettingRepository.findAllByCommunityBoardGroupSettings(SettingCategoryEnum.COMMUNITY_BOARD_GROUP);
    }

    /**
     * Retrieve all groups available for a member as select options.
     *
     * @param request Request containing the portal ID
     * @return List of group options
     */
    public List<IResponseSelectOption> findAllGroupForMemberSelectOption(RequestListGroupSelectOption request) {
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        if (Objects.isNull(portalId)) {
            portalId = PortalContextHolder.getPortalId();
        }
        var user = getCurrentUser(userRepository);
        var groupMemberId = user.getId();
        var groupRoles = Arrays.asList(
            CommunityBoardGroupRoleEnum.ORGANIZER,
            CommunityBoardGroupRoleEnum.MODERATOR,
            CommunityBoardGroupRoleEnum.MEMBER
        );
        if (isAdminOrPortalHostRole()) {
            groupMemberId = null;
        }
        return communityBoardGroupRepository.findAllGroupForMemberSelectOption(groupMemberId, portalId, groupRoles);
    }

    public Map<String, Object> getInvitedGroups(UUID userId, Pageable pageable) {
        var portalContext = PortalContextHolder.getContext();
        if (Objects.isNull(userId)) {
            userId = getCurrentUser(userRepository).getId();
        }
        UUID portalId = portalContext.getPortalId();
        var result = communityBoardGroupMemberRepository.findAllSendInviteByUserId(userId, portalId, pageable);
        return PageUtils.toPage(result);
    }

    /**
     * Retrieve the list of members available for invitation to a group.
     *
     * @param request  Request containing the group and portal IDs
     * @param pageable Pagination information
     * @return Paginated list of members available for invitation
     */
    public Map<String, Object> getListMemberInvite(RequestGetListMemberInvite request, Pageable pageable) {
        var user = getCurrentUser(userRepository);
        var groupId = UUIDUtils.toUUID(request.getGroupId());
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        if (Objects.isNull(portalId)) {
            portalId = PortalContextHolder.getPortalId();
        }
        var requestSearch = RequestGetUserInPortal.builder()
            .portalId(portalId)
            .ignoreUserId(user.getId())
            .groupId(groupId)
            .searchKeyword(StringUtils.lowerCase(request.getSearchKeyword()))
            .build();
        return PageUtils.toPage(memberRepository.getAllMemberToInviteGroup(requestSearch, pageable));
    }

    /**
     * Retrieve the list of join requests for a specific group.
     *
     * @param groupId  ID of the group
     * @param pageable Pagination information
     * @return Paginated list of join requests
     */
    public Map<String, Object> getRequestJoinGroup(UUID groupId, Pageable pageable) {
        var managerRole = List.of(CommunityBoardGroupRoleEnum.ORGANIZER, CommunityBoardGroupRoleEnum.MODERATOR);
        validateCurrentUserRole(groupId, managerRole);
        return PageUtils.toPage(communityBoardGroupMemberRepository.getMemberRequest(groupId, pageable));
    }

    /**
     * Accept a join request for a group by a manager.
     *
     * @param groupId  ID of the group
     * @param memberId ID of the member to approve
     */
    public void acceptJoinGroupByManager(UUID groupId, UUID memberId) {
        processJoinRequest(groupId, memberId, true);
    }

    /**
     * Reject a join request for a group by a manager.
     *
     * @param groupId  ID of the group
     * @param memberId ID of the member to reject
     */
    public void rejectJoinGroupByManager(UUID groupId, UUID memberId) {
        processJoinRequest(groupId, memberId, false);
    }

    /**
     * Validate GroupName Unique
     *
     * @param groupName String
     * @param excludeId UUID
     */
    private void validateGroupNameUnique(String groupName, UUID excludeId) {
        boolean isExists = excludeId == null
            ? communityBoardGroupRepository.existsByGroupNameIgnoreCase(groupName)
            : communityBoardGroupRepository.existsByGroupNameIgnoreCaseAndIdNot(groupName, excludeId);

        if (isExists) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Group Name"));
        }
    }

    /**
     * Save CommunityBoardGroup Settings
     *
     * @param communityBoardGroup CommunityBoardGroup
     * @param requests            List<RequestCommunityBoardGroupSetting>
     */
    private void saveCommunityBoardGroupSettings(
        CommunityBoardGroup communityBoardGroup,
        List<RequestCommunityBoardGroupSetting> requests
    ) {
        Map<String, SettingDefinition> definitionCache = new HashMap<>();

        List<CommunityBoardGroupSetting> communityBoardGroupSettings = requests
            .stream()
            .map(request -> processCommunityBoardGroupSetting(request, communityBoardGroup, definitionCache))
            .collect(Collectors.toList());

        communityBoardGroupSettingRepository.saveAll(communityBoardGroupSettings);
    }

    /**
     * Process CommunityBoardGroup Setting
     *
     * @param request             RequestCommunityBoardGroupSetting
     * @param communityBoardGroup CommunityBoardGroup
     * @param definitionCache     Map<String, SettingDefinition>
     * @return CommunityBoardGroupSetting
     */
    private CommunityBoardGroupSetting processCommunityBoardGroupSetting(
        RequestCommunityBoardGroupSetting request,
        CommunityBoardGroup communityBoardGroup,
        Map<String, SettingDefinition> definitionCache
    ) {
        SettingDefinition settingDefinition = definitionCache.computeIfAbsent(
            String.valueOf(request.getSettingKey()),
            this::getSettingDefinition
        );
        validateSettingValue(request.getSettingValue(), settingDefinition.getDataType());

        // Create or update CommunityBoardSetting
        CommunityBoardGroupSettingEmbedKey id = new CommunityBoardGroupSettingEmbedKey();
        id.setCommunityBoardGroup(communityBoardGroup);
        id.setSettingDefinition(settingDefinition);

        CommunityBoardGroupSetting communityBoardGroupSetting = communityBoardGroupSettingRepository
            .findById(id)
            .orElse(CommunityBoardGroupSetting.builder().id(id).build());

        communityBoardGroupSetting.setSettingKey(request.getSettingKey());
        communityBoardGroupSetting.setSettingValue(request.getSettingValue());

        return communityBoardGroupSetting;
    }

    /**
     * Get SettingDefinition
     *
     * @param keyCode String
     * @return SettingDefinition
     */
    private SettingDefinition getSettingDefinition(String keyCode) {
        return settingDefinitionRepository
            .findByKeyCode(SettingKeyCodeEnum.valueOf(keyCode))
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Setting Definition")));
    }

    /**
     * Validate SettingValue
     *
     * @param settingValue String
     * @param dataType     String
     */
    private void validateSettingValue(String settingValue, String dataType) {
        if ("json".equalsIgnoreCase(dataType)) {
            try {
                new ObjectMapper().readTree(settingValue);
            } catch (JsonProcessingException e) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0044, "settingValue"));
            }
        }
    }

    /**
     * Process Join Request
     *
     * @param groupId  UUID
     * @param memberId UUID
     * @param isAccept boolean
     */
    private void processJoinRequest(UUID groupId, UUID memberId, boolean isAccept) {
        // Check the role of the group manager
        validateCurrentUserRole(groupId, List.of(CommunityBoardGroupRoleEnum.ORGANIZER, CommunityBoardGroupRoleEnum.MODERATOR));

        // Check the role of the member in the group
        var groupMember = getGroupMemberByStatus(groupId, memberId, CommunityBoardGroupStatusEnum.REQUEST_JOIN);

        var member = userRepository.findById(memberId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Member")));
        var group = getCommunityBoardGroup(groupId);

        // Process request join
        if (isAccept) {
            groupMember.setGroupRole(CommunityBoardGroupRoleEnum.MEMBER);
            groupMember.setStatus(CommunityBoardGroupStatusEnum.JOINED);
            communityBoardGroupMemberRepository.save(groupMember);
            pushNotificationService.sendNotificationRequestJoinGroupAccepted(getCommunityBoardGroup(groupId), member.getId());
        } else {
            communityBoardGroupMemberRepository.delete(groupMember);
            pushNotificationService.sendNotificationRequestJoinGroupRejected(group, member.getId());
        }
    }

    /**
     * Get CommunityBoardGroup
     *
     * @param id UUID
     * @return CommunityBoardGroup
     */
    private CommunityBoardGroup getCommunityBoardGroup(UUID id) {
        return communityBoardGroupRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Community Board Group")));
    }
}
