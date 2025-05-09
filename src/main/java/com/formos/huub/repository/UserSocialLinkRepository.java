package com.formos.huub.repository;

import com.formos.huub.domain.entity.UserSocialLink;
import com.formos.huub.domain.enums.AuthProviderEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSocialLinkRepository extends JpaRepository<UserSocialLink, UUID> {

    Optional<UserSocialLink> findByProviderAndProviderUserId(AuthProviderEnum provider, String providerUserId);

    Optional<UserSocialLink> findByProviderAndUserId(AuthProviderEnum provider, UUID userId);

    List<UserSocialLink> findByUserId(UUID userId);

}
