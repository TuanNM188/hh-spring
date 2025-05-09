package com.formos.huub.repository;

import com.formos.huub.domain.entity.UserAnswerForm;
import com.formos.huub.domain.enums.EntryTypeEnum;
import com.formos.huub.domain.enums.FormCodeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAnswerFormRepository extends JpaRepository<UserAnswerForm, UUID> {

    List<UserAnswerForm> getAllByEntryIdAndEntryFormId(UUID entryId, UUID entryFormId);

    @Query(value = "select a from UserAnswerForm a join Question q on q.id = a.questionId " +
        "where a.entryId = :entryId and a.entryType = :entryType and q.formCode = :formCode")
    List<UserAnswerForm> getAllByEntryIdAndFormCode(UUID entryId, EntryTypeEnum entryType, FormCodeEnum formCode);

    @Query(value = "select a from UserAnswerForm a join Question q on q.id = a.questionId where " +
        "a.entryId is null and q.formCode = :formCode")
    List<UserAnswerForm> getAllByPortalIdAndFormDefaultData(FormCodeEnum formCode);

    @Query(value = "SELECT a FROM UserAnswerForm a JOIN Question q ON q.id = a.questionId " +
        "WHERE a.entryId = :entryId AND a.entryType = :entryType " +
        "AND (q.displayForm LIKE CONCAT('%', :displayForm, '%') OR q.formCode = 'PORTAL_INTAKE_ADDITIONAL_QUESTION')")
    List<UserAnswerForm> getAllBusinessOwnerAnswerByEntryIdAndEntryType(UUID entryId, EntryTypeEnum entryType, String displayForm);

    @Query(value = "SELECT a FROM UserAnswerForm a JOIN Question q ON q.id = a.questionId " +
        " JOIN PortalIntakeQuestion piq on q.id = piq.questionId " +
        "WHERE a.entryId = :entryId AND a.entryType = :entryType AND q.formCode = :formCode" +
        " AND piq.portalId = :portalId")
    List<UserAnswerForm> getAllAdditionalByUserAndPortalAndFormCode(UUID entryId, EntryTypeEnum entryType, UUID portalId, FormCodeEnum formCode);

    @Query(value = "SELECT a FROM UserAnswerForm a JOIN Question q ON q.id = a.questionId " +
        "WHERE a.entryId = :entryId AND a.entryType = :entryType " +
        "AND (q.questionCode IN (:questionCodes))")
    List<UserAnswerForm> getAllBusinessOwnerAnswerByEntryIdAndEntryTypeAndQuestionCode(UUID entryId, EntryTypeEnum entryType, List<String> questionCodes);

    @Query(value = "SELECT a FROM UserAnswerForm a JOIN Question q ON q.id = a.questionId " +
        "WHERE a.entryId = :entryId AND a.entryType = :entryType " +
        "AND (a.entryFormId = :entryFormId)")
    List<UserAnswerForm> getAllBusinessOwnerAnswerByEntryIdAndEntryTypeAndEntryFormId(UUID entryId, EntryTypeEnum entryType, UUID entryFormId);

    @Query("select uaf from UserAnswerForm uaf join Question q on uaf.questionId = q.id where q.parentId = :questionId " +
            " and uaf.entryId =:entryId and uaf.entryType = :entryType")
    List<UserAnswerForm> findAnswerOtherByQuestionIdAndEntryIdAndEntryType(UUID questionId, UUID entryId, EntryTypeEnum entryType);

    @Query("SELECT a FROM UserAnswerForm a " +
        "JOIN Question q ON q.id = a.questionId " +
        "WHERE a.entryId = :entryId AND a.entryType = :entryType AND q.questionCode = :questionCode")
    Optional<UserAnswerForm> findByQuestionCodeAndEntryIdAndEntryType(String questionCode, UUID entryId, EntryTypeEnum entryType);

}
