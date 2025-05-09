package com.formos.huub.repository;

import com.formos.huub.domain.entity.Question;
import com.formos.huub.domain.enums.FormCodeEnum;
import com.formos.huub.domain.enums.GroupCodeEnum;
import com.formos.huub.domain.response.answerform.IResponseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> getAllByFormCodeOrderByPriorityOrder(FormCodeEnum formCode);

    @Query(value = "SELECT q.id AS id, q.question AS question, q.questionType AS questionType, q.questionCode AS questionCode, q.groupCode AS groupCode, " +
        "q.priorityOrder AS priorityOrder, q.isRequire AS isRequire, q.formCode AS formCode, q.parentId AS parentId, q.optionType AS optionType, " +
        "q.allowCustomOptions AS allowCustomOptions, q.columnSize AS columnSize, p.isVisible AS isVisible, p.id AS portalIntakeQuestionId, " +
        "p.allowOtherInput AS allowOtherInput, q.placeholder AS placeholder, q.allowActionVisible AS allowActionVisible, q.description AS description " +
        "FROM Question q LEFT JOIN PortalIntakeQuestion p ON q.id = p.questionId AND p.portalId = :portalId " +
        "WHERE q.formCode = :formCode and q.parentId is null and q.questionCode NOT IN (:excludeQuestionCodes)")
    List<IResponseQuestion> getAllByFormCodeAndPortalId(FormCodeEnum formCode, UUID portalId, List<String> excludeQuestionCodes);

    @Query(value = "SELECT q.id AS id, q.question AS question, q.questionType AS questionType, q.questionCode AS questionCode, q.groupCode AS groupCode, " +
        "q.priorityOrder AS priorityOrder, q.isRequire AS isRequire, q.formCode AS formCode, q.parentId AS parentId, q.optionType AS optionType, " +
        "q.allowCustomOptions AS allowCustomOptions, q.columnSize AS columnSize, p.isVisible AS isVisible, p.id AS portalIntakeQuestionId, " +
        "p.allowOtherInput AS allowOtherInput, q.placeholder AS placeholder, q.allowActionVisible AS allowActionVisible, q.description AS description " +
        "FROM Question q JOIN PortalIntakeQuestion p ON q.id = p.questionId AND p.portalId = :portalId " +
        "WHERE q.questionCode IN (:questionCode)")
    List<IResponseQuestion> getAllByQuestionCodesAndPortalId(List<String> questionCode, UUID portalId);

    @Query("select q from Question q join PortalIntakeQuestion piq on q.id = piq.questionId and q.formCode = :formCode " +
        " where piq.portalId = :portalId order by q.priorityOrder asc")
    List<Question> getAllByPortalAndFormCode(UUID portalId, FormCodeEnum formCode);

    @Query("select " +
        "q.id AS id, q.question AS question, q.questionType AS questionType, q.questionCode AS questionCode, q.groupCode AS groupCode, " +
        "        q.priorityOrder AS priorityOrder, q.isRequire AS isRequire, q.formCode AS formCode, q.parentId AS parentId, q.optionType AS optionType, " +
        "        q.allowCustomOptions AS allowCustomOptions, q.columnSize AS columnSize, piq.isVisible AS isVisible, piq.id AS portalIntakeQuestionId, " +
        "        piq.allowOtherInput AS allowOtherInput, q.placeholder AS placeholder, q.allowActionVisible AS allowActionVisible, q.description AS description, q.inputType AS inputType, q.messageValidate as messageValidate " +
        " from Question q left join PortalIntakeQuestion piq on q.id = piq.questionId and piq.portalId = :portalId" +
        " left join Question qa on q.parentId = qa.id " +
        " left join PortalIntakeQuestion ppiq on qa.id = ppiq.questionId and ppiq.portalId = :portalId" +
        " where (coalesce(q.allowActionVisible, false) is false OR piq.isVisible is TRUE)" +
        " and (q.parentId is null OR ppiq.allowOtherInput is true )" +
        " and q.questionCode IN (:questionCodes) order by q.priorityOrder asc")
    List<IResponseQuestion> getAllConfigByQuestionCodesAndPortalId(List<String> questionCodes, UUID portalId);

    @Query("select " +
        "q.id AS id, q.question AS question, q.questionType AS questionType, q.questionCode AS questionCode, q.groupCode AS groupCode, " +
        "        q.priorityOrder AS priorityOrder, q.isRequire AS isRequire, q.formCode AS formCode, q.parentId AS parentId, q.optionType AS optionType, " +
        "        q.allowCustomOptions AS allowCustomOptions, q.columnSize AS columnSize, " +
        "        q.placeholder AS placeholder, q.allowActionVisible AS allowActionVisible, q.description AS description, q.inputType AS inputType, q.messageValidate as messageValidate " +
        " from Question q " +
        " where q.questionCode IN (:questionCodes) order by q.priorityOrder asc")
    List<IResponseQuestion> getAllByQuestionCodes(List<String> questionCodes);

    List<Question> getAllByQuestionCodeIn(List<String> questionCodes);

    @Query(value = """
            SELECT q.*
            FROM question q
            WHERE q.question_code = ANY(STRING_TO_ARRAY(:questionCodes, ','))
            AND q.is_delete IS FALSE
            ORDER BY ARRAY_POSITION(STRING_TO_ARRAY(:questionCodes, ','), q.question_code);
        """, nativeQuery = true)
    List<Question> getAllByQuestionCodeInOrderBy(@Param("questionCodes") String questionCodes);

    List<Question> findAllByFormCode(FormCodeEnum formCode);

    @Modifying
    void deleteAllByIdIn(List<UUID> ids);

    @Query(value = "SELECT q.id AS id, q.question AS question, q.questionType AS questionType, q.questionCode AS questionCode, q.groupCodeForMember AS groupCode, " +
        "(CASE WHEN :displayForm = 'BUSINESS_OWNER_FORM' THEN q.priorityOrderForMemberForm WHEN :displayForm = 'BUSINESS_OWNER_DETAILS_MODAL' THEN q.priorityOrderForBusinessOwnerForm END) AS priorityOrder, " +
        "q.isRequire AS isRequire, q.formCode AS formCode, q.parentId AS parentId, q.optionType AS optionType, " +
        "q.allowCustomOptions AS allowCustomOptions, " +
        "(CASE WHEN :displayForm = 'BUSINESS_OWNER_FORM' THEN q.columnSizeForMemberForm WHEN :displayForm = 'BUSINESS_OWNER_DETAILS_MODAL' THEN q.columnSizeForBusinessOwnerForm END) AS columnSize, " +
        "p.isVisible AS isVisible, p.id AS portalIntakeQuestionId, " +
        "p.allowOtherInput AS allowOtherInput, q.placeholder AS placeholder, q.allowActionVisible AS allowActionVisible, q.description AS description, " +
        "q.messageValidate AS messageValidate, q.inputType AS inputType " +
        "FROM Question q LEFT JOIN PortalIntakeQuestion p ON q.id = p.questionId AND p.portalId = :portalId " +
        "WHERE q.groupCodeForMember IN (:memberGroupCode) AND q.displayForm LIKE CONCAT('%', :displayForm, '%')")
    List<IResponseQuestion> getAllBusinessOwnerQuestionByCodesAndPortalId(List<GroupCodeEnum> memberGroupCode, UUID portalId, String displayForm);

}
