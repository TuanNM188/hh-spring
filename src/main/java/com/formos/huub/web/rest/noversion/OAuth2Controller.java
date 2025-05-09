/**
 * ***************************************************
 * * Description :
 * * File        : OAuth2Controller
 * * Author      : Hung Tran
 * * Date        : Oct 21, 2024
 * ***************************************************
 **/
package com.formos.huub.web.rest.noversion;

import com.formos.huub.domain.enums.SocialProviderEnum;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.oauth2.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OAuth2Controller {

    ResponseSupport responseSupport;
    OAuth2Service oAuth2Service;

    @GetMapping("/google/callback")
    public void googleCallback(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "state", required = false) String state,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        try {
            String result = oAuth2Service.handleOAuthCallback(code, state, SocialProviderEnum.GOOGLE);
            if (Objects.isNull(result)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            } else {
                response.sendRedirect(result);
            }
        } catch (Exception e) {
            oAuth2Service.writeClosePopupResponse(response);
        }
    }

    @GetMapping("/facebook/callback")
    public void facebookCallback(
        @RequestParam(required = false) String code,
        @RequestParam(value = "state", required = false) String state,
        HttpServletResponse response
    ) throws IOException {
        try {
            String result = oAuth2Service.handleOAuthCallback(code, state, SocialProviderEnum.FACEBOOK);
            if (Objects.isNull(result)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            } else {
                response.sendRedirect(result);
            }
        } catch (Exception e) {
            oAuth2Service.writeClosePopupResponse(response);
        }
    }

    @GetMapping("/microsoft/callback")
    public void processMicrosoftCallback(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "state", required = false) String state,
        HttpServletResponse response
    ) throws IOException {
        try {
            String result = oAuth2Service.handleOAuthCallback(code, state, SocialProviderEnum.MICROSOFT);
            if (Objects.isNull(result)) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            } else {
                response.sendRedirect(result);
            }
        } catch (Exception e) {
            oAuth2Service.writeClosePopupResponse(response);
        }
    }

    @GetMapping("/token-info/{stateToken}")
    public ResponseEntity<ResponseData> getTokenInfo(@PathVariable String stateToken) {
        var result = oAuth2Service.getTokenInfoFromCache(stateToken);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }
}
