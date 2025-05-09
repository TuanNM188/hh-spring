package com.formos.huub.service.metricsreport;

import com.formos.huub.framework.properties.MetabaseProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.jhipster.config.JHipsterProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetabaseTokenService {

    private static final String PARAM_RESOURCE = "resource";
    private static final String PARAM_DASHBOARD = "dashboard";
    private static final String PARAMS = "params";
    private static final String PARAM_EXP = "exp";
    private static final String PARAM_PORTAL_NAME = "portal_name";
    private static final String PARAM_TIMEZONE = "timezone";

    private final MetabaseProperties metabaseProperties;

    public String getIframeUrl(long dashboardId, String portalName, String timezone) {
        var validity = (System.currentTimeMillis() / 1000) + (24 * 60 * 60);
        Map<String, Object> params = new HashMap<>();
        //TODO request param to metabase
        params.put(PARAM_PORTAL_NAME, portalName);
//        params.put(PARAM_TIMEZONE, timezone);

        Map<String, Object> payload = new HashMap<>();
        payload.put(PARAM_RESOURCE, Map.of(PARAM_DASHBOARD, dashboardId));
        payload.put(PARAMS, params);
        payload.put(PARAM_EXP, validity);

        String token = Jwts.builder()
            .setClaims(payload)
            .signWith(SignatureAlgorithm.HS256, metabaseProperties.getSecretKey().getBytes())
            .compact();

        return metabaseProperties.getSiteUrl() + "/embed/dashboard/" + token + "#bordered=true&titled=true";
    }
}
