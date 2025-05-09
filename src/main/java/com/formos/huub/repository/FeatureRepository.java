package com.formos.huub.repository;

import com.formos.huub.domain.entity.Feature;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.response.feature.IResponseFeatureNavigation;
import com.formos.huub.domain.response.feature.IResponseFeaturePortalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, UUID> {

    @Query("select f.id as id, f.name as name, f.featureCode as featureCode," +
        " f.description as description, f.priorityOrder as priorityOrder, f.routerLink as routerLink, f.isDynamic is false as isActive, f.featureKey as featureKey, f.groupKey as groupKey " +
        " from Feature f left join Feature p on f.parentId = p.id where p.featureCode = :featureCode order by f.priorityOrder asc")
    List<IResponseFeatureNavigation> getAllByParentCode(FeatureCodeEnum featureCode);


    @Query("select f from Feature f left join Feature p on f.parentId = p.id where p.featureCode IN :featureCodes or (f.isDynamic is true)")
    List<Feature> getAllByFeatureCodeIn(List<FeatureCodeEnum> featureCodes);

    @Query("SELECT f.id AS id, f.description AS name, false AS isActive, f.featureCode as featureCode, f.featureKey as featureKey " +
        "FROM Feature f " +
        "WHERE f.isDynamic IS true ORDER BY f.description ASC")
    List<IResponseFeaturePortalSetting> getAllFeaturePortalSetting();

    @Query("SELECT f.id AS id, f.description AS name, pf.isActive AS isActive, f.featureCode as featureCode, f.featureKey as featureKey" +
        " FROM PortalFeature pf  " +
        " JOIN pf.id.feature f ON f.id = pf.id.feature.id join pf.id.portal p " +
        " WHERE p.id = :portalId AND f.isDynamic IS TRUE ORDER BY f.description ASC")
    List<IResponseFeaturePortalSetting> getAllFeaturePortalSettingByPortalId(UUID portalId);

    @Query("""
        SELECT p
        FROM PortalFeature pf
        JOIN pf.id.feature f
        JOIN pf.id.portal p
        WHERE f.featureCode = :featureCode and pf.isActive = true
        group by p.id
    """)
    List<Portal> getAllPortalIdsEnableEventCalendar(FeatureCodeEnum featureCode);

    @Query("""
        SELECT EXISTS (
            SELECT 1
            FROM PortalFeature pf
            JOIN pf.id.feature f on f.id = pf.id.feature.id
            WHERE f.featureCode = :featureCode and pf.isActive = true and pf.id.portal.id = :portalId
        )
    """)
    boolean existsPortalIdEnableEventCalendar(UUID portalId, FeatureCodeEnum featureCode);


}
