package com.formos.huub.repository;

import com.formos.huub.domain.entity.PortalFeature;
import com.formos.huub.domain.entity.embedkey.PortalFeatureEmbedKey;
import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.response.feature.IResponseFeatureNavigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortalFeatureRepository extends JpaRepository<PortalFeature, PortalFeatureEmbedKey> {


    @Query("select f.id as id, f.name as name, f.featureCode as featureCode," +
        " f.description as description, f.priorityOrder as priorityOrder, f.routerLink as routerLink , coalesce(pt.isActive, true) as isActive, " +
        " pa.featureCode as parentFeatureCode, f.groupCode as groupCode, f.groupName as groupName, f.featureKey as featureKey, f.groupKey as groupKey" +
        " from Feature f LEFT JOIN PortalFeature pt ON f.id = pt.id.feature.id and pt.id.portal.id = :portalId" +
        " left join Feature pa on f.parentId = pa.id " +
        " where (pa.featureCode = :featureCode " +
        " And (f.featureCode NOT IN (:excludeFeatureCodes))) " +
        " OR (f.featureCode IN (:includeFeatureCodes)) " +
        " order by f.priorityOrder asc")
    List<IResponseFeatureNavigation> getAllByParentCodeAndPortalId(FeatureCodeEnum featureCode, UUID portalId, List<FeatureCodeEnum> excludeFeatureCodes,
                                                                   List<FeatureCodeEnum> includeFeatureCodes);


    @Modifying
    @Query("DELETE FROM PortalFeature pf WHERE pf.id.portal.id = :portalId")
    void deleteAllOnPortalFeatureByPortalId(UUID portalId);

    @Query("select pf from PortalFeature pf join pf.id.portal p join pf.id.feature f where p.id = :portalId and f.id IN :featureIds")
    List<PortalFeature> findAllByPortalIdAndFeatureIds(UUID portalId, List<UUID> featureIds);

    Optional<PortalFeature> findById_PortalIdAndId_FeatureId(UUID portalId, UUID featureId);

    Optional<PortalFeature> findById_Feature_FeatureCodeAndId_Portal_Id(FeatureCodeEnum featureCode, UUID portalId);

}
