package com.formos.huub.framework.support;

import com.formos.huub.framework.properties.CustomDomainProperties;
import com.google.common.net.InternetDomainName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * ***************************************************
 * * Description :
 * * File        : DomainSupport
 * * Author      : Hung Tran
 * * Date        : Sep 28, 2024
 * ***************************************************
 **/

@Slf4j
@Component
public class DomainSupport {

    private static final String LOCALHOST = "localhost";
    private static final String IP_LOCAL = "192.168.1.65";
    private static final String DOT_LOCALHOST = "." + LOCALHOST;
    private static final String WWW = "www";
    private static final Set<String> DEV_ENVIRONMENTS = Set.of("dev");
    private static final String HTTP_PREFIX = "http://";

    @Autowired
    private Environment environment;

    @Autowired
    private CustomDomainProperties customDomainProperties;

    public Optional<String> extractSubdomain(String origin) {
        return Optional.ofNullable(origin)
            .filter(o -> !o.isEmpty())
            .map(String::toLowerCase)
            .flatMap(this::extractHostFromOrigin)
            .flatMap(host -> isDevEnvironment() ? handleLocalhostInDev(host) : extractSubdomainFromHost(host));
    }

    public boolean isMainDomain(String domain) {
        log.info("Checking if domain is main domain: {}", domain);
        List<String> mainDomains = customDomainProperties.getPrimaryDomain();

        if (domain == null) return false;

        // Remove http:// or https:// prefix
        String normalizedDomain = domain.replaceAll("^https?://", "");

        return mainDomains
            .stream()
            .anyMatch(mainDomain -> {
                // Exact match
                if (normalizedDomain.equals(mainDomain)) {
                    return true;
                }

                // Check subdomain pattern
                String domainPattern = "^[\\w-]+\\." + Pattern.quote(mainDomain) + "$";
                return Pattern.compile(domainPattern).matcher(normalizedDomain).matches();
            });
    }

    private Optional<String> extractHostFromOrigin(String origin) {
        try {
            URI uri = new URI(origin);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                uri = new URI(HTTP_PREFIX + origin);
                host = uri.getHost();
            }
            return Optional.ofNullable(host);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    private boolean isDevEnvironment() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch(DEV_ENVIRONMENTS::contains);
    }

    private Optional<String> handleLocalhostInDev(String host) {
        if (host.equals(LOCALHOST) || host.equals(IP_LOCAL)) {
            return Optional.empty();
        } else if (host.endsWith(DOT_LOCALHOST)) {
            String subdomain = host.substring(0, host.length() - DOT_LOCALHOST.length());
            return Optional.of(subdomain)
                .filter(s -> !s.isEmpty())
                .filter(s -> customDomainProperties.getTreatWwwAsSubdomain() || !s.equals(WWW));
        }
        return Optional.empty();
    }

    private Optional<String> extractSubdomainFromHost(String host) {
        log.info("Extracting subdomain from host: {}", host);
        try {
            // List of special domains
            List<String> specialDomains = Arrays.asList("huub-staging.formos.org", "huub-uat.formos.org", "huub.formos.org");

            // Check if host ends with one of the special domains
            for (String specialDomain : specialDomains) {
                if (host.endsWith(specialDomain)) {
                    String subdomainPart = host.substring(0, host.length() - specialDomain.length());
                    // Remove the dot at the end if any
                    if (subdomainPart.endsWith(".")) {
                        subdomainPart = subdomainPart.substring(0, subdomainPart.length() - 1);
                    }
                    // Return subdomain if not empty and not "www" (depending on treatWwwAsSubdomain)
                    return Optional.of(subdomainPart)
                        .filter(s -> !s.isEmpty())
                        .filter(s -> customDomainProperties.getTreatWwwAsSubdomain() || !s.equals(WWW));
                }
            }

            // Handle normally for other cases
            InternetDomainName domainName = InternetDomainName.from(host);
            if (!domainName.isUnderPublicSuffix()) {
                return Optional.empty();
            }

            String registrableDomain = domainName.topPrivateDomain().toString();
            String fullDomain = domainName.toString();

            // Check if there is a subdomain
            if (fullDomain.length() <= registrableDomain.length()) {
                return Optional.empty();
            }

            String subdomainPart = fullDomain.substring(0, fullDomain.length() - registrableDomain.length() - 1);

            return Optional.of(subdomainPart)
                .filter(s -> !s.isEmpty())
                .filter(s -> customDomainProperties.getTreatWwwAsSubdomain() || !s.equals(WWW));
        } catch (Exception e) {
            log.error("Error while extracting subdomain from host", e);
            return Optional.empty();
        }
    }
}
