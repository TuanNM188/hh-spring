package com.formos.huub.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.dto.AdminUserDTO;
import com.formos.huub.domain.dto.UserDTO;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.entity.embedkey.UserSettingEmbedKey;
import com.formos.huub.domain.enums.PortalHostStatusEnum;
import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.request.account.KeyAndPasswordRequest;
import com.formos.huub.domain.request.account.RequestUpdatePassword;
import com.formos.huub.domain.request.user.RequestUpdateProfile;
import com.formos.huub.domain.request.usersetting.RequestUserSetting;
import com.formos.huub.domain.response.member.ResponseTechnicalAdvisor;
import com.formos.huub.domain.response.member.ResponseTechnicalAdvisorSetting;
import com.formos.huub.domain.response.technicaladvisor.IResponseTechnicalAdvisorSetting;
import com.formos.huub.domain.response.user.ResponseProfile;
import com.formos.huub.domain.response.usersetting.IResponseUserSetting;
import com.formos.huub.domain.response.usersetting.ResponseUserSetting;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.*;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.mapper.technicaladvisor.TechnicalAdvisorMapper;
import com.formos.huub.mapper.user.UserMapper;
import com.formos.huub.mapper.usersetting.UserSettingMapper;
import com.formos.huub.repository.*;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.pushnotification.PushNotificationService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing users.
 */
@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService extends BaseService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthorityRepository authorityRepository;
    CacheManager cacheManager;
    IMailService mailService;
    ApplicationProperties applicationProperties;
    PortalHostRepository portalHostRepository;
    UserMapper userMapper;
    UserSettingRepository userSettingRepository;
    SettingDefinitionRepository settingDefinitionRepository;
    UserSettingMapper userSettingMapper;
    TechnicalAdvisorRepository technicalAdvisorRepository;
    PortalRepository portalRepository;
    TechnicalAdvisorMapper technicalAdvisorMapper;
    CommunityPartnerRepository communityPartnerRepository;
    BusinessOwnerRepository businessOwnerRepository;
    PushNotificationService pushNotificationService;

    public Optional<User> verifyPasswordResetLink(String resetKey) {
        log.info("Verify password reset link for reset key {}", resetKey);
        Optional<User> userOpt = userRepository.findOneByResetKey(resetKey);
        if (userOpt.isEmpty()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0023));
        }
        User user = userOpt.get();
        long resetPasswordTokenExpireIn = applicationProperties.getResetPassword().getResetKeyValidityInSeconds();
        Instant resetDateExpire = user.getResetDate().plus(resetPasswordTokenExpireIn, ChronoUnit.SECONDS);
        if (Instant.now().isAfter(resetDateExpire)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0021));
        }
        return userOpt;
    }

    public Optional<User> completePasswordReset(String newPassword, String resetKey) {
        log.info("Reset user password for reset key {}", resetKey);
        User user = this.verifyPasswordResetLink(resetKey).get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetKey(null);
        user.setResetDate(null);
        pushNotificationService.sendNotifyToUserOfChangedPassword(user.getId());
        this.clearUserCaches(user);
        return Optional.of(user);
    }

    public String requestPasswordReset(String mail) {
        Optional<User> userOpt = userRepository.findOneByLogin(mail);
        if (userOpt.isEmpty()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0025));
        }
        String resetKey = RandomUtil.generateResetKey();
        User user = userOpt.get();
        user.setResetKey(resetKey);
        user.setResetDate(Instant.now());
        this.clearUserCaches(user);
        var portalContext = PortalContextHolder.getContext();
        AtomicReference<String> subdomain = new AtomicReference<>();
        if (Objects.nonNull(portalContext.getPortalId())) {
            portalRepository
                .findById(portalContext.getPortalId())
                .ifPresent(ele -> {
                    subdomain.set(ele.getUrl());
                });
        }

        mailService.sendPasswordResetMail(mail, resetKey, subdomain.get(), portalContext.getPrimaryLogo());
        return resetKey;
    }

    public void activateInvitedMember(KeyAndPasswordRequest request) {
        User user = this.verifyResetPasswordLink(request.getKey()).get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setActivated(true);
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setResetKey(null);
        user.setResetDate(null);

        // Send notification to user
        pushNotificationService.sendNotifyToUserOfChangedPassword(user.getId());

        // Clear user caches
        this.clearUserCaches(user);
    }

    public Optional<User> verifyResetPasswordLink(String resetKey) {
        Optional<User> userOpt = userRepository.findOneByResetKey(resetKey);
        if (userOpt.isEmpty()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0023));
        }
        User user = userOpt.get();
        long inviteTokenExpireIn = applicationProperties.getInviteTechnicalAdvisor().getInviteTokenValidityInSeconds();
        Instant resetDateExpire = user.getResetDate().plus(inviteTokenExpireIn, ChronoUnit.SECONDS);
        if (Instant.now().isAfter(resetDateExpire)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0021));
        }
        return userOpt;
    }

    /**
     * Get Profile detail by userId
     *
     * @param userId UUID
     * @return ResponseProfile
     */
    public ResponseProfile getProfileByUserId(UUID userId) {
        User user = getUser(userId);
        var response = userMapper.toResponse(user);
        getMemberResponseByRole(user, response);
        return response;
    }

    private void getMemberResponseByRole(User user, ResponseProfile responseMemberDetail) {
        var authority = getMemberRole(user);
        switch (authority) {
            case AuthoritiesConstants.SYSTEM_ADMINISTRATOR:
            case AuthoritiesConstants.PORTAL_HOST:
            case AuthoritiesConstants.COMMUNITY_PARTNER:
            case AuthoritiesConstants.BUSINESS_OWNER:
                break;
            case AuthoritiesConstants.TECHNICAL_ADVISOR:
                TechnicalAdvisor technicalAdvisor = user.getTechnicalAdvisor();
                IResponseTechnicalAdvisorSetting technicalAdvisorSetting = technicalAdvisorRepository.getTechnicalAdvisorSetting(
                    technicalAdvisor.getId()
                );
                ResponseTechnicalAdvisorSetting responseSettings = technicalAdvisorMapper.toResponseFromInterface(technicalAdvisorSetting);
                ResponseTechnicalAdvisor responseTechnicalAdvisor = new ResponseTechnicalAdvisor();
                responseTechnicalAdvisor.setId(technicalAdvisor.getId());
                responseTechnicalAdvisor.setTechnicalAdvisorSetting(responseSettings);
                responseMemberDetail.setTechnicalAdvisor(responseTechnicalAdvisor);
                break;
            default:
                throw new IllegalArgumentException("Invalid role value");
        }
    }

    private void getIdRoleByMember(User user, AdminUserDTO userDTO) {
        var authority = getMemberRole(user);
        switch (authority) {
            case AuthoritiesConstants.SYSTEM_ADMINISTRATOR:
                break;
            case AuthoritiesConstants.PORTAL_HOST:
                portalHostRepository
                    .findByUserIdAndPortalId(user.getId(), userDTO.getPortalId())
                    .ifPresent(portalHost -> userDTO.setCommunityPartnerId(portalHost.getId()));
                break;
            case AuthoritiesConstants.COMMUNITY_PARTNER:
                var communityPartner = user.getCommunityPartner();
                if (Objects.nonNull(user.getCommunityPartner())) {
                    communityPartnerRepository
                        .findById(communityPartner.getId())
                        .ifPresent(cp -> userDTO.setCommunityPartnerId(cp.getId()));
                }
                break;
            case AuthoritiesConstants.BUSINESS_OWNER:
                businessOwnerRepository
                    .findByUserId(user.getId())
                    .ifPresent(businessOwner -> userDTO.setBusinessOwnerId(businessOwner.getId()));
                break;
            case AuthoritiesConstants.TECHNICAL_ADVISOR:
                technicalAdvisorRepository.findByUserId(user.getId()).ifPresent(ta -> userDTO.setTechnicalAdvisorId(ta.getId()));
                break;
            default:
                throw new IllegalArgumentException("Invalid role value");
        }
    }

    /**
     * Update profile
     *
     * @param userId  UUID
     * @param request RequestUpdateProfile
     */
    public void updateProfile(UUID userId, RequestUpdateProfile request) {
        Optional<User> userOpt = userRepository.findOneByEmailIgnoreCase(request.getEmail()).filter(user -> !user.getId().equals(userId));
        if (userOpt.isPresent()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Email"));
        }
        User user = getUser(userId);
        userMapper.partialEntity(user, request);
        user.makeNormalizedFullName();
        userRepository.save(user);
        insertUserSettings(user.getId());
        clearUserCaches(user);
    }

    /**
     * Update Profile Password
     *
     * @param request RequestUpdatePassword
     */
    public void updateProfilePassword(RequestUpdatePassword request) {
        User user = getCurrentUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // Send notification to user
        pushNotificationService.sendNotifyToUserOfChangedPassword(user.getId());
        clearUserCaches(user);
    }

    /**
     * Get All UserSettings
     *
     * @return List<ResponseSettingCategory>
     */
    public List<IResponseUserSetting> getAllUserSettings(SettingCategoryEnum category) {
        User user = getCurrentUser();
        return userSettingRepository.findAllByUserIdAndCategory(user.getId(), category);
    }

    /**
     * Update UserSettings
     * @param requests List<RequestUserSetting>
     */
    public List<ResponseUserSetting> updateUserSettings(List<RequestUserSetting> requests) {
        User currentUser = getCurrentUser();
        Map<String, SettingDefinition> definitionCache = new HashMap<>();

        List<UserSetting> userSettings = requests
            .stream()
            .map(request -> processUserSetting(request, currentUser, definitionCache))
            .collect(Collectors.toList());

        userSettingRepository.saveAll(userSettings);
        return userSettingMapper.toListResponse(userSettings);
    }

    private UserSetting processUserSetting(RequestUserSetting request, User user, Map<String, SettingDefinition> definitionCache) {
        SettingDefinition settingDefinition = definitionCache.computeIfAbsent(
            String.valueOf(request.getSettingKey()),
            this::getSettingDefinition
        );
        validateSettingValue(request.getSettingValue(), settingDefinition.getDataType());

        // Create or update UserSetting
        UserSettingEmbedKey id = new UserSettingEmbedKey();
        id.setUser(user);
        id.setSettingDefinition(settingDefinition);

        UserSetting userSetting = userSettingRepository.findById(id).orElse(UserSetting.builder().id(id).build());

        userSetting.setSettingKey(request.getSettingKey());
        userSetting.setSettingValue(request.getSettingValue());

        return userSetting;
    }

    private SettingDefinition getSettingDefinition(String keyCode) {
        return settingDefinitionRepository
            .findByKeyCode(SettingKeyCodeEnum.valueOf(keyCode))
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Setting Definition")));
    }

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
     * Insert User Settings
     *
     * @param userId UUID
     */
    public void insertUserSettings(UUID userId) {
        userRepository.insertUserSettings(userId);
    }

    /**
     * Mark account as first logged in
     */
    public void markAccountAsFirstLoggedIn() {
        User user = getCurrentUser();
        user.setFirstLoggedIn(true);
        userRepository.save(user);
        clearUserCaches(user);
    }

    /**
     * Get current User
     *
     * @return User
     */
    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
    }

    /**
     * Get user userId
     *
     * @param userId UUID
     * @return User
     */
    private User getUser(UUID userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
    }

    public User registerUser(AdminUserDTO userDTO, String password) {
        userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new UsernameAlreadyUsedException();
                }
            });
        userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .ifPresent(existingUser -> {
                boolean removed = removeNonActivatedUser(existingUser);
                if (!removed) {
                    throw new EmailAlreadyUsedException();
                }
            });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        log.info("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    public User createUser(AdminUserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(AppConstants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = userDTO
                .getAuthorities()
                .stream()
                .map(authorityRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        this.clearUserCaches(user);

        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin().toLowerCase());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                if (userDTO.getEmail() != null) {
                    user.setEmail(userDTO.getEmail().toLowerCase());
                }
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                this.clearUserCaches(user);
                log.info("Changed Information for User: {}", user);
                return user;
            })
            .map(AdminUserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository
            .findOneByLogin(login)
            .ifPresent(user -> {
                userRepository.delete(user);
                this.clearUserCaches(user);
                log.info("Deleted User: {}", user);
            });
    }

    private String getMemberRole(User user) {
        return user
            .getAuthorities()
            .stream()
            .findFirst()
            .map(Authority::getName)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Role")));
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                userRepository.save(user);
                this.clearUserCaches(user);
                log.info("Changed Information for User: {}", user);
            });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }
                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                log.info("Changed password for User: {}", user);
            });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getUserWithAuthorities() {
        var userOptional = SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin);
        AdminUserDTO result = userOptional
            .map(AdminUserDTO::new)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.PORTAL_HOST)) {
            portalHostRepository
                .findByUserIdAndStatus(result.getId(), PortalHostStatusEnum.ACTIVE)
                .ifPresent(portalHost -> {
                    var portal = portalHost.getPortal();
                    if (Objects.nonNull(portal)) {
                        result.setPortalId(portalHost.getPortal().getId());
                    }
                });
        }
        getIdRoleByMember(userOptional.get(), result);
        return result;
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
            .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
            .forEach(user -> {
                log.info("Deleting not activated user {}", user.getLogin());
                userRepository.delete(user);
                this.clearUserCaches(user);
            });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).toList();
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }
}
