package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.authenticate.*;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.ValidationUtils;
import com.formos.huub.service.tokenmanager.TokenManagerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to authenticate users.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateController extends BaseController {

    ResponseSupport responseSupport;
    TokenManagerService tokenManagerService;
    Validator validator;

    @PostMapping("/sign-in")
    public ResponseEntity<ResponseData> authorize(@Valid @RequestBody SignInRequest request) {
        Set<ConstraintViolation<SignInRequest>> violations;
        if (request.getProvider() == null) {
            violations = validator.validate(request, EmailLogin.class, Default.class);
        } else {
            violations = validator.validate(request, SocialLogin.class, Default.class);
        }
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(ValidationUtils.buildValidationErrors(violations));
        }
        return responseSupport.success(ResponseData.builder().data(tokenManagerService.signIn(request)).build());
    }

    @PostMapping("/sign-in-as-user")
    @PreAuthorize("hasPermission(null, 'SIGN_IN_AS_USER')")
    public ResponseEntity<ResponseData> signInAsUser(@RequestBody RequestSignInAsUser request) {
        return responseSupport.success(ResponseData.builder().data(tokenManagerService.signInAsUser(request)).build());
    }

    @GetMapping("/sign-in-as-user/{stateToken}/token-info")
    public ResponseEntity<ResponseData> signInAsUserTokenInfo(@PathVariable String stateToken) {
        return responseSupport.success(ResponseData.builder().data(tokenManagerService.getDataTokenFromCache(stateToken)).build());
    }

    @PostMapping("/refresh-token")
    @PreAuthorize("hasPermission(null, 'REFRESH_TOKEN')")
    @ResponseBody
    public ResponseEntity<ResponseData> refreshToken(@Valid @RequestBody RequestTokenRefresh requestTokenRefresh) {
        return responseSupport.success(ResponseData.builder().data(tokenManagerService.refreshToken(requestTokenRefresh)).build());
    }

    @GetMapping("/sign-out")
    @PreAuthorize("hasPermission(null, 'SIGN_OUT')")
    public ResponseEntity<ResponseData> logoutUser() {
        tokenManagerService.signOut();
        return responseSupport.success(MessageHelper.getMessage(Message.Keys.I0006, "Sign out"));
    }

    @PostMapping("/switch-portal")
    @PreAuthorize("hasPermission(null, 'SWITCH_PORTAL')")
    public ResponseEntity<ResponseData> switchPortal(@RequestBody RequestSwitchPortal request) {
        return responseSupport.success(ResponseData.builder().data(tokenManagerService.switchPortal(request)).build());
    }
}
