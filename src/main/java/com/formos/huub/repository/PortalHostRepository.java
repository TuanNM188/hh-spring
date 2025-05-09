package com.formos.huub.repository;

import com.formos.huub.domain.entity.PortalHost;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.PortalHostStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PortalHostRepository extends JpaRepository<PortalHost, UUID> {


    Optional<PortalHost> findByEmailAndStatus(String email, PortalHostStatusEnum status);

    Optional<PortalHost> findByEmailIgnoreCase(String email);

    Optional<PortalHost> findByInviteToken(String inviteToken);

    Optional<PortalHost> findByUserIdAndStatus(UUID userId, PortalHostStatusEnum status);

    Optional<PortalHost> findByIdAndStatus(UUID id, PortalHostStatusEnum status);

    Set<PortalHost> getAllByPortalIdOrIdIn(UUID portalId, List<UUID> ids);

    Optional<PortalHost> findByUserIdAndPortalId(UUID userId, UUID portalId);

    Optional<PortalHost> findByUserId(UUID userId);

    @Query("SELECT count(ph) > 0 FROM PortalHost ph WHERE ph.isPrimary IS true AND ph.userId = :memberId")
    Boolean existPrimaryPortalHostByMemberId(UUID memberId);

    List<PortalHost> getAllByPortalId(UUID portalId);

    boolean existsByPortalIdAndEmail(UUID portalId, String email);

    List<PortalHost> getAllByPortalIdInAndStatus(List<UUID> portalIds, PortalHostStatusEnum status);

    @Query("SELECT u FROM PortalHost ph join ph.portal p join User u on u.id = ph.userId WHERE  ph.isPrimary IS true AND p.id = :portalId")
    Optional<User> findPortalHostPrimaryByPortal(UUID portalId);


}
