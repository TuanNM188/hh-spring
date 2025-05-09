package com.formos.huub.repository;

import com.formos.huub.domain.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {

    Optional<Invite> findByInviteToken(String inviteToken);

    Optional<Invite> findByEmailIgnoreCase(String email);

}
