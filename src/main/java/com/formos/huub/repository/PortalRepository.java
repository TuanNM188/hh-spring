package com.formos.huub.repository;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.enums.PortalStatusEnum;
import com.formos.huub.domain.request.member.RequestSearchMember;
import com.formos.huub.domain.response.member.IResponseSearchMember;
import com.formos.huub.domain.response.portals.IResponseLocation;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.sound.sampled.Port;

@Repository
public interface PortalRepository extends JpaRepository<Portal, UUID>, PortalRepositoryCustom {
    boolean existsByPlatformName(@NotNull String name);

    @Query("SELECT p.id AS id, p.platformName AS name FROM Portal p where p.status = 'ACTIVE' order by p.platformName asc")
    List<IResponseLocation> getAll();

    @Query("SELECT count(p) > 0 FROM Portal p WHERE lower(p.url) = lower(:subdomain)")
    Boolean existsBySubdomain(String subdomain);

    @Query("SELECT count(p) > 0 FROM Portal p WHERE lower(p.url) = lower(:subdomain) AND p.id != :id")
    Boolean existsBySubdomainAndNotEqualId(String subdomain, UUID id);

    @Query(
        value = "select " +
            " count(1) > 0 " +
            "from portal_state ps " +
            "left join  location ls on ps.state_id = ls.geo_name_id and ls.location_type = 'STATE' " +
            "left join location lc on ps.cities like concat('%',lc.geo_name_id,'%') and lc.location_type = 'CITY' " +
            "left join location lz on " +
            " ps.zipcodes like concat('%',lz.geo_name_id,'%') and lz.location_type = 'ZIPCODE' " +
            " where ps.is_delete is false and ps.portal_id = :portalId and (:state is null or ls.code = :state)" +
            " and lc.code = :city and lz.code = :zipCode",
        nativeQuery = true
    )
    Boolean checkSupportingPortal(
        @Param("portalId") UUID portalId,
        @Param("city") String city,
        @Param("state") String state,
        @Param("zipCode") String zipCode
    );

    @Query(
        value = "select " +
        " distinct p.*  " +
        "from portal_state ps " +
        "inner join portal p on p.id = ps.portal_id and p.is_delete is false " +
        "left join  location ls on ps.state_id = ls.geo_name_id and ls.location_type = 'STATE' " +
        "left join location lc on ps.cities like concat('%',lc.geo_name_id,'%') and lc.location_type = 'CITY' " +
        "left join location lz on " +
        " ps.zipcodes like concat('%',lz.geo_name_id,'%') and lz.location_type = 'ZIPCODE' " +
        " where ps.is_delete is false and p.status = 'ACTIVE' and ls.code = :state and lc.code = :city and lz.code = :zipCode",
        nativeQuery = true
    )
    List<Portal> findPortalSupportingLocation(String city, String state, String zipCode);

    Optional<Portal> getPortalByUrlIgnoreCaseAndIsCustomDomainAndStatus(String url, boolean isCustomDomain, PortalStatusEnum status);

    Optional<Portal> findByIdAndStatus(UUID portalId, PortalStatusEnum status);

    @Query(value = "select p.id from program_term pt join program pr on pr.id =  pt.program_id and pt.is_delete is false" +
        " join portal p on p.id =  pr.portal_id and p.is_delete is false " +
        " where pt.status = 'ACTIVE'" +
        " group by p.id having count(pt.id) > 0", nativeQuery = true)
    List<UUID> getPortalIdHaveProgramTermActive();

    @Query(value = """
            SELECT p.id AS id, p.platform_name AS name
                FROM portal p JOIN portal_community_partner pc
                    ON p.id = pc.portal_id AND p.is_delete = FALSE JOIN community_partner c
                    ON c.id = pc.community_partner_id AND c.is_delete = FALSE
                WHERE c.id = :communityPartnerId
        """, nativeQuery = true)
    List<IResponseLocation> getPortalByCommunityPartnerId(@Param("communityPartnerId") UUID communityPartnerId);
}
