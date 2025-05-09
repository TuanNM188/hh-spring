package com.formos.huub.repository;

import com.formos.huub.domain.entity.CalendarEvent;
import com.formos.huub.domain.entity.Funding;
import com.formos.huub.domain.request.funding.RequestSearchFunding;
import com.formos.huub.domain.response.funding.IResponseSearchFunding;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FundingRepository extends JpaRepository<Funding, UUID>, PortalFundingRepositoryCustom {

    String QUERY_SEARCH_FUNDING_BY_CONDITIONS = "select f.id as id, f.title as title, f.description as description," +
        " f.amount as amount, f.date_added as dateAdded," +
        " f.type as type, f.image_url as imageUrl, f.application_deadline as applicationDeadline, " +
        " f.funding_categories as fundingCategories, (CASE WHEN fs.id is not null THEN true ELSE false END) as isSubmitted, " +
        " (CASE WHEN uf.id is not null THEN true ELSE false END) as isFavorite, f.status as status," +
        " f.application_url as applicationUrl" +
        " from funding f " +
        " left join funding_portal fp on f.id = funding_id" +
        " left join user_favorite uf on  (uf.user_id IS NOT DISTINCT FROM :#{#cond.userId}) and uf.entry_id = f.id and " +
        " uf.favorite_type = 'FUNDING' and uf.status = 'ACTIVE'" +
        " left join funding_submitted fs on (fs.user_id IS NOT DISTINCT FROM :#{#cond.userId}) and fs.funding_id = f.id " +
        " and fs.status = 'ACTIVE'" +
        " where f.is_delete is false" +
        " and (coalesce(:#{#cond.portalId}, null ) is null or fp.portal_id = :#{#cond.portalId})" +
        " and (coalesce(:#{#cond.excludeIds}, null ) is null or f.id not in (:#{#cond.excludeIds}))" +
        " and (coalesce(:#{#cond.isFavorite}, null ) is null or (CASE WHEN uf.id is not null THEN true ELSE false END) = :#{#cond.isFavorite})" +
        " and (coalesce(:#{#cond.searchKeyword}, null ) is null " +
        " or (lower(f.title) like lower(concat('%',coalesce(:#{#cond.searchKeyword}, ''),'%')))" +
        "   OR LOWER(TRIM(BOTH ' ' FROM REGEXP_REPLACE(f.description, :regexHtml, ' ', 'g'))) " +
        "        LIKE LOWER(CONCAT('%', COALESCE(:#{#cond.searchKeyword}, ''), '%')))" +
        " and (coalesce(:#{#cond.fundingTypes}, null ) is null or f.type IN (SELECT UNNEST(string_to_array(:#{#cond.fundingTypes}, ','))))" +
        " and (coalesce(:#{#cond.status}, null ) is null or f.status IN (SELECT UNNEST(string_to_array(:#{#cond.status}, ','))))" +
        " and (coalesce(:#{#cond.amountMin}, null ) is null " +
        " or (coalesce(:#{#cond.amountMin}, null ) is not null and coalesce(:#{#cond.amountMax}, null ) is not null and f.amount between :#{#cond.amountMin} and :#{#cond.amountMax})" +
        " or (coalesce(:#{#cond.amountMin}, null ) is not null and coalesce(:#{#cond.amountMax}, null ) is null and f.amount >= :#{#cond.amountMin}))" +
        " and (coalesce(:#{#cond.filterStatus}, null ) is null " +
        " or ('EXPIRING_SOON' IN (SELECT UNNEST(string_to_array(:#{#cond.filterStatus}, ','))) " +
        " and TO_CHAR(f.application_deadline, 'YYYY-MM-DD') between :#{#cond.currentDate} and :#{#cond.endExpiringSoonDate})" +
        " or ('RECENTLY_ADDED' IN (SELECT UNNEST(string_to_array(:#{#cond.filterStatus}, ','))) " +
        " and TO_CHAR(f.date_added, 'YYYY-MM-DD') between :#{#cond.startRecentlyAdded} and :#{#cond.currentDate})" +
        " or ('EXPIRED' IN (SELECT UNNEST(string_to_array(:#{#cond.filterStatus}, ','))) and TO_CHAR(f.application_deadline, 'YYYY-MM-DD') < :#{#cond.currentDate}))" +
        " and (coalesce(:#{#cond.fundingPage}, null ) is null " +
        " or ('ALL_FUNDING' = :#{#cond.fundingPage} and f.status = 'ACTIVE' and TO_CHAR(f.publish_date, 'YYYY-MM-DD') <= :#{#cond.currentDate})" +
        " or ('FUNDING_MY_FAVORITE' = :#{#cond.fundingPage} and (CASE WHEN uf.id is not null THEN true ELSE false END) = true)" +
        " or ('FUNDING_SUBMITTED' = :#{#cond.fundingPage} and (CASE WHEN fs.id is not null THEN true ELSE false END) = true))" +
        " AND (coalesce(:#{#cond.fundingCategories}, null ) is null " +
        "       or string_to_array(f.funding_categories , ',') && string_to_array(:#{#cond.fundingCategories} , ',')) " +
        "group by f.id, uf.id, fs.id";

    @Query(value = QUERY_SEARCH_FUNDING_BY_CONDITIONS,
        countQuery = "select count(1) from (" + QUERY_SEARCH_FUNDING_BY_CONDITIONS + ") as temp", nativeQuery = true)
    Page<IResponseSearchFunding> searchByConditions(@Param("cond") RequestSearchFunding requestSearchFunding, String regexHtml, Pageable pageable);

    @Query(value = "select f.id as id, f.title as title, f.description as description," +
        " f.amount as amount, f.date_added as dateAdded," +
        " f.type as type, f.image_url as imageUrl, f.application_deadline as applicationDeadline, " +
        " f.funding_categories AS fundingCategories, " +
        " f.status as status, f.application_url as applicationUrl" +
        " from funding f " +
        " left join funding_portal fp on f.id = funding_id" +
        " where f.is_delete is false" +
        " AND (coalesce(:#{#cond.portalId}, null ) is null or fp.portal_id = :#{#cond.portalId})" +
        " AND (coalesce(:#{#cond.excludeIds}, null ) is null or f.id::TEXT != ALL(string_to_array(:#{#cond.excludeIds}, ',')))" +
        " AND (coalesce(:#{#cond.fundingCategories}, null ) is null " +
        "        or string_to_array(f.funding_categories , ',') && string_to_array(:#{#cond.fundingCategories} , ','))" +
        " group by f.id" +
        " ORDER BY  " +
        "    CASE  " +
        "        WHEN DATE(f.application_deadline) >= CURRENT_DATE THEN 0 " +
        "        ELSE 1 " +
        "    END ASC, " +
        "    f.application_deadline ASC " +
        "LIMIT 3" , nativeQuery = true)
    List<IResponseSearchFunding> getRelatedFunding(@Param("cond") RequestSearchFunding requestSearchFunding);

    boolean existsByTitle(@NotNull String name);

    @Query(value = "SELECT count(fp) > 0 FROM funding_portal fp WHERE fp.portal_id = :portalId", nativeQuery = true)
    Boolean existByPortalId(UUID portalId);


    @Query(value = "select f.* from funding f join funding_portal fp on fp.funding_id = f.id and f.is_delete is false" +
        " where f.status = 'ACTIVE' and fp.portal_id = :portalId" +
        " ORDER BY  DATE(f.date_added) desc, f.publish_date asc", nativeQuery = true)
    List<Funding> findLatestByPortal(UUID portalId, Pageable pageable);

}
