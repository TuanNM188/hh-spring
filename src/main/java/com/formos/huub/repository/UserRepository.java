package com.formos.huub.repository;

import com.formos.huub.domain.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.formos.huub.domain.response.communitypartner.IResponseCommunityPartnerStaff;
import com.formos.huub.domain.response.portals.IResponseWelcomeMessageSender;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";
    Optional<User> findOneByActivationKey(String activationKey);
    List<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);
    Optional<User> findOneByResetKey(String resetKey);
    Optional<User> findOneByEmailIgnoreCase(String email);
    Optional<User> findOneByLogin(String login);
    Optional<User> findOneByLoginIgnoreCase(String login);
    Boolean existsByLoginIgnoreCase(String login);

    @Query(value = "SELECT COUNT(u) > 0 " +
        "FROM jhi_user u " +
        "WHERE lower(u.login) = lower(:login)", nativeQuery = true)
    Boolean existsByLoginIgnoreCaseIncludeDeleted(String login);

    @Query(value = "SELECT u.* " +
        "FROM jhi_user u " +
        "WHERE lower(u.login) = lower(:login)", nativeQuery = true)
    Optional<User> findOneByEmail(String login);

    Boolean existsByUsernameIgnoreCase(String username);
    Boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = { "authorities", "authorities.permissions" })
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_EMAIL_CACHE)
    Optional<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Page<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    @Query("select u from User u join u.authorities a where u.email = :email and a.name = :role")
    Optional<User> findOneByEmailAndRole(String email, String role);

    @Query("select u from User u where lower(u.email) = lower(:email) and u.id != :id ")
    Optional<User> findOneByEmailIgnoreCaseAndNotId(String email);

    @Procedure(name = "User.insertUserSettings")
    void insertUserSettings(@Param("userId") UUID userId);

    @Query(value = "SELECT username" +
        " FROM jhi_user ju " +
        " WHERE username LIKE concat(:username,'%') " +
        "  AND username ~ concat('^',:username,'[0-9]+$')" +
        " order by username desc limit 1", nativeQuery = true)
    String getLastUsernameByName(String username);

    @Query("SELECT u FROM User u LEFT JOIN u.technicalAdvisor ta WHERE u.id = :userOrTechnicalAdvisorId OR ta.id = :userOrTechnicalAdvisorId")
    Optional<User> findByUserIdOrTechnicalAdvisorId(UUID userOrTechnicalAdvisorId);

    @Query("SELECT u FROM User u JOIN u.authorities a " +
        "WHERE u.communityPartner.id = :communityPartnerId " +
        "AND u.isNavigator IS true " +
        "AND a.name = 'ROLE_COMMUNITY_PARTNER'")
    Optional<User> findCommunityPartnerNavigatorById(UUID communityPartnerId);

    @Modifying
    @Query("UPDATE User u SET u.isNavigator = false WHERE u.communityPartner.id = :communityPartnerId AND u.id != :userId")
    void updateUserNavigator(UUID communityPartnerId, UUID userId);

    @Query("SELECT u FROM User u JOIN u.authorities a " +
        "WHERE u.communityPartner.id = :communityPartnerId " +
        "AND a.name = 'ROLE_COMMUNITY_PARTNER'")
    List<User> getAllCommunityPartnerStaffByCommunityPartnerId(UUID communityPartnerId);

    @Query(value = "SELECT u.id AS id, u.firstName AS firstName, u.lastName AS lastName, u.email AS email, " +
        "u.status AS status, u.normalizedFullName AS normalizedFullName, u.imageUrl AS imageUrl, " +
        "coalesce(u.isNavigator, false) AS isNavigator, coalesce(u.isPrimary, false) AS isPrimary " +
        "FROM User u " +
        "WHERE u.communityPartner.id = :communityPartnerId")
    List<IResponseCommunityPartnerStaff> getCommunityPartnerStaffs(UUID communityPartnerId);

    @Query(value = "select count(1) > 0 as isExist from jhi_user u where u.username = :username", nativeQuery = true)
    Boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) > 0 " +
        "FROM User u JOIN u.communityPartner cp JOIN cp.portals p " +
        "WHERE u.id = :userId AND p.id = :portalId")
    boolean existsMemberCommunityPartnerByUserIdAndPortalId(UUID userId, UUID portalId);

    @Query("SELECT DISTINCT u.id AS userId, u.normalizedFullName AS name " +
        "FROM User u " +
        "JOIN u.authorities au " +
        "LEFT JOIN PortalHost ph ON u.id = ph.userId " +
        "WHERE u.status = 'ACTIVE' " +
        "AND ((:portalId IS NULL AND au.name = 'ROLE_SYSTEM_ADMINISTRATOR') " +
        "OR (:portalId IS NOT NULL AND au.name = 'ROLE_PORTAL_HOST' AND ph.portal.id = :portalId) " +
        "OR au.name = 'ROLE_SYSTEM_ADMINISTRATOR')")
    List<IResponseWelcomeMessageSender> getWelcomeMessageSenders(UUID portalId);

    @Modifying
    @Query("UPDATE User u SET u.isPrimary = false WHERE u.communityPartner.id = :communityPartnerId AND u.id != :userId")
    void updateUserPrimary(UUID communityPartnerId, UUID userId);

    @Query("SELECT u FROM User u JOIN u.authorities a " +
        "WHERE u.communityPartner.id = :communityPartnerId " +
        "AND u.isPrimary IS true " +
        "AND a.name = 'ROLE_COMMUNITY_PARTNER'")
    Optional<User> getCommunityPartnerPrimaryById(UUID communityPartnerId);

}
