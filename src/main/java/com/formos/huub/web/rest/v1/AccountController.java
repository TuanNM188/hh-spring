package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.dto.AdminUserDTO;
import com.formos.huub.domain.dto.PasswordChangeDTO;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.AuthProviderEnum;
import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.request.account.KeyAndPasswordRequest;
import com.formos.huub.domain.request.account.RequestLinkSocialAccount;
import com.formos.huub.domain.request.account.RequestUpdatePassword;
import com.formos.huub.domain.request.account.SignUpUserRequest;
import com.formos.huub.domain.request.bookingsetting.RequestBookingSetting;
import com.formos.huub.domain.request.communitypartner.RequestUpdateCommunityPartner;
import com.formos.huub.domain.request.user.RequestUpdateProfile;
import com.formos.huub.domain.request.useranswerform.RequestBusinessOwnerAnswerForm;
import com.formos.huub.domain.request.usersetting.RequestUserSetting;
import com.formos.huub.domain.response.account.ResponseUserSocialLink;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.mail.IMailService;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.bookingsetting.BookingSettingService;
import com.formos.huub.service.communitypartner.CommunityPartnerService;
import com.formos.huub.service.portals.PortalService;
import com.formos.huub.service.technicaladvisor.TechnicalAdvisorService;
import com.formos.huub.service.user.UserService;
import com.formos.huub.service.useranswerform.UserFormService;
import com.formos.huub.service.usersociallink.UserSocialLinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController extends BaseController {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    UserRepository userRepository;
    UserService userService;
    IMailService mailService;
    ResponseSupport responseSupport;
    TechnicalAdvisorService technicalAdvisorService;
    PortalService portalService;
    UserSocialLinkService userSocialLinkService;
    CommunityPartnerService communityPartnerService;
    BookingSettingService bookingSettingService;
    UserFormService userFormService;

    /**
     * {@code POST  /register} : register the user.
     *
     * @param signUpUserRequest the managed user View Model.
     * @throws BadRequestException {@code 400 (Bad Request)} if the password or username is incorrect.
     */
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody SignUpUserRequest signUpUserRequest) {
        if (isPasswordLengthInvalid(signUpUserRequest.getPassword())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0021, "Password"), new Throwable());
        }
        User user = userService.registerUser(signUpUserRequest, signUpUserRequest.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  } : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("")
    //    @PreAuthorize("hasPermission(null, 'GET_ACCOUNT')")
    public ResponseEntity<ResponseData> getAccount() {
        var result = userService.getUserWithAuthorities();
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    /**
     * {@code POST  } : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws BadRequestException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException    {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @PostMapping("")
    public void saveAccount(@Valid @RequestBody AdminUserDTO userDTO) {
        String userLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.orElseThrow().getLogin().equalsIgnoreCase(userLogin))) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0022, "Email"), new Throwable());
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (user.isEmpty()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUser(
            userDTO.getFirstName(),
            userDTO.getLastName(),
            userDTO.getEmail(),
            userDTO.getLangKey(),
            userDTO.getImageUrl()
        );
    }

    /**
     * {@code POST  /change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws BadRequestException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0021, "Password"), new Throwable());
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /verify-invite-token/{token}} : Verify invitation link.
     *
     * @param token the invitation token.
     */
    @GetMapping(path = "/verify-invite-token/{token}")
    public ResponseEntity<ResponseData> verifyInviteAccountTechnicalAdvisorLink(@PathVariable String token) {
        technicalAdvisorService.verifyInviteTechnicalAdvisorLink(token);
        return responseSupport.success(MessageHelper.getMessage(Message.Keys.I0001));
    }

    /**
     * {@code POST  /activate} : activate the portal host account.
     *
     * @param request the generated key and the new password.
     */
    @PostMapping("/portal-host/activate")
    public ResponseEntity<ResponseData> activatePortalHostAccount(@Valid @RequestBody KeyAndPasswordRequest request) {
        if (isPasswordLengthInvalid(request.getNewPassword())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Password"));
        }
        portalService.createUserPortalHost(request);
        return responseSupport.success(MessageHelper.getMessage(Message.Keys.I0001));
    }

    /**
     * {@code POST   /reset-password/init} : Email reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/reset-password/init")
    public ResponseEntity<ResponseData> requestPasswordReset(@RequestBody @Email String mail) {
        String resetKey = userService.requestPasswordReset(mail);
        return responseSupport.success(ResponseData.builder().data(resetKey).build());
    }

    /**
     * {@code POST   /reset-password/{token}} : Verify password reset link.
     *
     * @param token the reset password token.
     */
    @GetMapping(path = "/reset-password/{token}")
    public ResponseEntity<ResponseData> verifyPasswordResetLink(@PathVariable String token) {
        userService.verifyPasswordResetLink(token);
        return responseSupport.success(MessageHelper.getMessage(Message.Keys.I0001));
    }

    /**
     * {@code POST   /reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/reset-password/finish")
    public ResponseEntity<ResponseData> finishPasswordReset(@Valid @RequestBody KeyAndPasswordRequest keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Password"));
        }
        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());
        return responseSupport.success(ResponseData.builder().data(user.get().getLogin()).build());
    }

    @PostMapping("/invited-member/activate")
    public ResponseEntity<ResponseData> activateInvitedMemberAccount(@Valid @RequestBody KeyAndPasswordRequest request) {
        if (isPasswordLengthInvalid(request.getNewPassword())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Password"));
        }
        userService.activateInvitedMember(request);
        return responseSupport.success();
    }

    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasPermission(null, 'GET_PROFILE_BY_USER_ID')")
    public ResponseEntity<ResponseData> findProfileByUserId(@PathVariable @UUIDCheck String userId) {
        ResponseData result = ResponseData.builder().data(userService.getProfileByUserId(UUID.fromString(userId))).build();
        return responseSupport.success(result);
    }

    @PatchMapping("/profile/{userId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_PROFILE_BY_USER_ID')")
    public ResponseEntity<ResponseData> updateProfile(
        @PathVariable @UUIDCheck String userId,
        @RequestBody @Valid RequestUpdateProfile request
    ) {
        userService.updateProfile(UUID.fromString(userId), request);
        return responseSupport.success();
    }

    @PostMapping("/profile/update-password")
    @PreAuthorize("hasPermission(null, 'UPDATE_PROFILE_PASSWORD')")
    public ResponseEntity<ResponseData> updateProfilePassword(@RequestBody @Valid RequestUpdatePassword request) {
        userService.updateProfilePassword(request);
        return responseSupport.success();
    }

    @PostMapping("/social/connect")
    @PreAuthorize("hasPermission(null, 'LINK_SOCIAL_ACCOUNT')")
    public ResponseEntity<ResponseData> linkSocialAccount(@Valid @RequestBody RequestLinkSocialAccount request) {
        List<ResponseUserSocialLink> result = userSocialLinkService.linkSocialAccount(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @DeleteMapping("/social/disconnect")
    @PreAuthorize("hasPermission(null, 'UNLINK_SOCIAL_ACCOUNT')")
    public ResponseEntity<ResponseData> unlinkSocialAccount(@RequestParam String provider) {
        List<ResponseUserSocialLink> result = userSocialLinkService.unlinkSocialAccount(AuthProviderEnum.valueOf(provider));
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/social/{userId}")
    @PreAuthorize("hasPermission(null, 'GET_USER_SOCIAL_LINK_BY_USER_ID')")
    public ResponseEntity<ResponseData> getListUserSocialLinkByUserId(@PathVariable @UUIDCheck String userId) {
        List<ResponseUserSocialLink> result = userSocialLinkService.getListUserSocialLinkByUserId(UUID.fromString(userId));
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/user-settings")
    @PreAuthorize("hasPermission(null, 'GET_USER_NOTIFICATION_PRIVACY_SETTINGS')")
    public ResponseEntity<ResponseData> getUserSettings(@RequestParam String category) {
        var result = userService.getAllUserSettings(SettingCategoryEnum.valueOf(category));
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @PatchMapping("/user-settings")
    @PreAuthorize("hasPermission(null, 'UPDATE_USER_NOTIFICATION_PRIVACY_SETTINGS')")
    public ResponseEntity<ResponseData> updateUserSettings(@RequestBody @Valid List<RequestUserSetting> request) {
        var result = userService.updateUserSettings(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @GetMapping("/community-partner/{communityPartnerId}")
    @PreAuthorize("hasPermission(null, 'MEMBER_PROFILE_GET_COMMUNITY_PARTNER_BY_ID')")
    public ResponseEntity<ResponseData> getCommunityPartnerOrg(@PathVariable String communityPartnerId) {
        var response = communityPartnerService.getDetailCommunityPartner(UUID.fromString(communityPartnerId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/community-partner/{communityPartnerId}")
    @PreAuthorize("hasPermission(null, 'MEMBER_PROFILE_UPDATE_COMMUNITY_PARTNER')")
    public ResponseEntity<ResponseData> updateCommunityPartnerOrg(
        @RequestBody RequestUpdateCommunityPartner request,
        @PathVariable String communityPartnerId
    ) {
        communityPartnerService.updateCommunityPartner(UUID.fromString(communityPartnerId), request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/booking")
    @PreAuthorize("hasPermission(null, 'MEMBER_PROFILE_SAVE_BOOKING_SETTING')")
    public ResponseEntity<ResponseData> saveBookingSetting(@RequestBody RequestBookingSetting request)
        throws IOException, URISyntaxException {
        bookingSettingService.updateBookingSetting(request);
        return responseSupport.success();
    }

    @PostMapping("/{userId}/business-owner-answer")
    @PreAuthorize("hasPermission(null, 'PROFILE_BUSINESS_OWNER_ANSWER')")
    public ResponseEntity<ResponseData> fillBusinessOwnerFormByUser(
        @PathVariable @UUIDCheck String userId,
        @RequestBody RequestBusinessOwnerAnswerForm request,
        @RequestParam String displayForm
    ) {
        userFormService.fillFormBusinessOwner(UUID.fromString(userId), request.getBusinessOwner(), displayForm);
        return responseSupport.success();
    }

    @PostMapping("/mark-as-first-login")
    public ResponseEntity<ResponseData> markAccountAsFirstLoggedIn() {
        userService.markAccountAsFirstLoggedIn();
        return responseSupport.success();
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
            password.length() < SignUpUserRequest.PASSWORD_MIN_LENGTH ||
            password.length() > SignUpUserRequest.PASSWORD_MAX_LENGTH
        );
    }
}
