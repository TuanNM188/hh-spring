package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.response.learninglibrary.IResponseCourseExport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningLibraryRepository extends JpaRepository<LearningLibrary, UUID>, LearningLibraryRepositoryCustom {

    @Query(value = """
        select
            p.platform_name as portalName,
            TO_CHAR(llr.created_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as createdDate,
            u.first_name as firstName,
            u.last_name as lastName,
            u.email as email,
            bn.businessName,
            ll."name" as courseName,
            c."name" as categoryName,
            TO_CHAR(llr.registration_date at TIME zone 'UTC' at TIME zone :timezone ,'MM/DD/YYYY') as registrationDate,
            TO_CHAR(lb.completionDate at TIME zone 'UTC' at TIME zone :timezone,'MM/DD/YYYY') as completionDate,
            coalesce(ull.rating, 0) as rating,
            coalesce(lb.numCompleted, 0) as numCompleted,
            coalesce(ls.totalLessons, 0) as totalLessons
        from learning_library_registration llr
        inner join learning_library ll on ll.id = llr.learning_library_id and llr.is_delete is false and ll.is_delete is false
        inner join jhi_user u on u.id = llr.user_id and u.status = 'ACTIVE'
        inner join business_owner bo on bo.user_id = u.id
        left join portal p on p.id = bo.portal_id
        left join category c on c.id = ll.category_id
        left join (
            select
                uaf.entry_id as userId,
                uaf.additional_answer as businessName
            from
                user_answer_form uaf join question q on uaf.question_id = q.id
            where uaf.entry_type = 'USER'
                and uaf.is_delete is false
                and q.question_code = 'PORTAL_INTAKE_QUESTION_BUSINESS'
            group by q.id, uaf.id) as bn on bn.userId = u.id
         left join (
             select llr2.id as learning_library_registration_id, count(case when llrd.learning_status = 'COMPLETE' then 1 end ) as numCompleted,
             max(llrd.completion_date) as completionDate
             from learning_library_registration llr2
             left join learning_library_registration_detail llrd on llrd.learning_library_registration_id = llr2.id and llrd.is_delete is false
             group by llr2.id
         ) as lb on lb.learning_library_registration_id = llr.id
          left join (
             select ll2.id as learning_library_id, count(lll.id) as totalLessons from learning_library ll2
             left join learning_library_step lls on lls.learning_library_id = ll2.id and lls.is_delete is false
             left join learning_library_lesson lll on lll.learning_library_step_id = lls.id and lll.is_delete is false
             where ll2.is_delete is false
             group by ll2.id
         ) as ls on ls.learning_library_id = ll.id
         left join  user_learning_library ull on u.id = ull.user_id and ull.learning_library_id = ll.id
         where (:portalId is null or p.id = :portalId)
         and (:startDate is null
         OR TO_CHAR(llr.created_date AT TIME ZONE 'UTC' AT TIME ZONE :timezone, 'YYYY-MM-DD') between :startDate and :endDate)
         order by llr.created_date desc
        """, nativeQuery = true)
    List<IResponseCourseExport> getAllByPortalAndDate(UUID portalId, String startDate, String endDate, String timezone);


    @Query(
        "select count(l) > 0 " +
            "from LearningLibrary l " +
            "join User u on u.id = l.userCreatedId " +
            "where u.login = :username and l.id = :learningLibraryId"
    )
    boolean existsByUsername(String username, UUID learningLibraryId);

    @Query("SELECT count(l) > 0 " +
        "FROM LearningLibrary l " +
        "JOIN l.portals p " +
        "WHERE p.id = :portalId and l.id = :learningLibraryId"
    )
    boolean existsByPortalIdAndLearningLibraryId(UUID portalId, UUID learningLibraryId);

    @Query(value = "select lb.* from learning_library lb join learning_library_portal llp " +
        " on llp.learning_library_id = lb.id and lb.is_delete is false " +
        " where lb.status = 'PUBLISHED' and llp.portal_id = :portalId " +
        " ORDER BY DATE(lb.created_date) desc, lb.start_date asc ", nativeQuery = true)
    List<LearningLibrary> findLatestByPortal(UUID portalId, Pageable pageable);

}
