package com.formos.huub.repository;

import com.formos.huub.domain.entity.AnswerOption;
import com.formos.huub.domain.enums.FormCodeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {

    @Query(value = "select a from AnswerOption a join a.question q where q.formCode = :formCode")
    List<AnswerOption> getAllByForm(FormCodeEnum formCode);

    @Query(value = "select a from AnswerOption a join a.question q where q.id = :questionId")
    List<AnswerOption> getAllByQuestion(UUID questionId);

    @Query(value = "SELECT a FROM AnswerOption a JOIN a.question q WHERE q.id = :questionId AND (a.entryId IS null OR a.entryId = :portalId)")
    List<AnswerOption> getAllByQuestionAndPortalId(UUID questionId, UUID portalId);

    @Query(value = "SELECT a FROM AnswerOption a WHERE a.entryId = :portalId AND a.question.id = :questionId AND a.isExtra IS true")
    List<AnswerOption> getAllExtraAnswerByPortalId(UUID portalId, UUID questionId);

    @Modifying
    void deleteAllByQuestionId(UUID questionId);

    List<AnswerOption> getAllByIdIn(List<UUID> optionIds);
}
