package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardLike;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import com.formos.huub.domain.response.communityboardreaction.IResponseCommunityBoardReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBoardLikeRepository extends JpaRepository<CommunityBoardLike, UUID> {
    List<CommunityBoardLike> findByAuthorIdAndEntryIdAndEntryType(UUID authorId, UUID entryId, CommunityBoardEntryTypeEnum entryType);

    @Query("""
        SELECT cbl.id as id, json_build_object('id', u.id, 'avatar', u.imageUrl,'fullName', u.normalizedFullName) as author
        FROM CommunityBoardLike cbl
        JOIN User u on u.id = cbl.authorId
        WHERE cbl.entryId = :entryId
    """)
    List<IResponseCommunityBoardReaction> findAllByEntryId(UUID entryId);
}
