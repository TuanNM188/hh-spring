package com.formos.huub.config;

import com.formos.huub.domain.entity.Authority;
import com.formos.huub.domain.entity.Permission;
import com.formos.huub.domain.entity.User;
import com.formos.huub.repository.UserRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    @Autowired
    public CustomJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String principalClaimValue = jwt.getSubject();
        Collection<GrantedAuthority> authorities = getAuthorities(principalClaimValue);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    @Cacheable(value = "userAuthoritiesCache", key = "#principalClaimValue")
    public Collection<GrantedAuthority> getAuthorities(String principalClaimValue) {
        Collection<GrantedAuthority> authorities = new HashSet<>();
        Optional<User> userOpt = userRepository.findOneByEmailIgnoreCase(principalClaimValue);
        if (userOpt.isPresent()) {
            for (Authority authority : userOpt.get().getAuthorities()) {
                authorities.add(new SimpleGrantedAuthority(authority.getName()));
                for (Permission permission : authority.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                }
            }
        }
        return authorities;
    }
}
