package com.formos.huub.security.jwt;

import static com.formos.huub.security.SecurityUtils.JWT_ALGORITHM;

import com.formos.huub.domain.entity.Authority;
import com.formos.huub.domain.entity.Permission;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.management.SecurityMetersService;
import com.formos.huub.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import tech.jhipster.config.JHipsterProperties;

@Component
public class TokenProvider {

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private static final String ADMIN_USERNAME = "adminUsername";

    private static final String PORTAL_ID = "portalId";

    private static final String INVALID_JWT_TOKEN = "Invalid JWT token {}";

    private static final String JWT_TOKEN_HAS_EXPIRED = "Token has expired: {}";

    private static final String UNSUPPORTED_JWT_TOKEN = "Unsupported JWT token: {}";

    private static final String MALFORMED_JWT_TOKEN = "Malformed JWT token: {}";

    private static final String INVALID_JWT_SIGNATURE = "Invalid JWT signature: {}";

    private static final String TOKEN_VALIDATION_ERROR = "Token validation error: {}";

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;

    private final SecurityMetersService securityMetersService;
    private final UserRepository userRepository;

    public TokenProvider(
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        JHipsterProperties jHipsterProperties,
        SecurityMetersService securityMetersService,
        CacheManager cacheManager,
        UserRepository userRepository
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;

        this.tokenValidityInMilliseconds = jHipsterProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInMillisecondsForRememberMe = jHipsterProperties
            .getSecurity()
            .getAuthentication()
            .getJwt()
            .getTokenValidityInSecondsForRememberMe();
        this.securityMetersService = securityMetersService;
        this.userRepository = userRepository;
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        //var authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Instant now = Instant.now();
        Instant validity = now.plus(this.tokenValidityInMilliseconds, ChronoUnit.SECONDS);
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            //.claim(AUTHORITIES_KEY, authorities)
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createTokenSignInAsUser(Authentication authentication, String adminUser, UUID portalId) {
        //var authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Instant now = Instant.now();
        Instant validity = now.plus(this.tokenValidityInMilliseconds, ChronoUnit.SECONDS);
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            //.claim(AUTHORITIES_KEY, authorities)
            .claim(ADMIN_USERNAME, adminUser)
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public Authentication getAuthentication(String token) {
        var jwt = jwtDecoder.decode(token);
        /*
        var claims = jwt.getClaims();

        Collection<? extends GrantedAuthority> authorities = Arrays
            .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .filter(auth -> !auth.trim().isEmpty())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        */
        String username = jwt.getSubject();
        Optional<com.formos.huub.domain.entity.User> userOpt = userRepository.findOneByEmailIgnoreCase(username);
        if (userOpt.isPresent()) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            for (Authority authority : userOpt.get().getAuthorities()) {
                authorities.add(new SimpleGrantedAuthority(authority.getName()));
                for (Permission permission : authority.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
            User principal = new User(username, "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        }
        throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0009));
    }

    public boolean validateToken(String authToken) {
        return  !isTokenExpired(authToken);
    }

    public Authentication getAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Jwt::getSubject);
    }

    public Instant getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Jwt::getExpiresAt);
    }

    public <T> T getClaimFromToken(String token, Function<Jwt, T> claimsResolver) {
        final Jwt claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Jwt getAllClaimsFromToken(String token) {
        return jwtDecoder.decode(token);
    }

    public boolean isTokenExpired(String token) {
        return Objects.requireNonNull(getExpirationDateFromToken(token)).isBefore(Instant.now());
    }
}
