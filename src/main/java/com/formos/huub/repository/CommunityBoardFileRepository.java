package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardFile;
import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import com.formos.huub.domain.enums.CommunityBoardFileTypeEnum;
import com.formos.huub.domain.response.communityboard.IResponseCommunityBoardFile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBoardFileRepository extends JpaRepository<CommunityBoardFile, UUID> {
    List<CommunityBoardFile> findAllByEntryTypeAndEntryId(CommunityBoardEntryTypeEnum entryType, UUID entryId);

    @Query("""
        SELECT cbf.id as id, cbf.path as url, cbf.type as mediaType, cbf.realName as name, (CASE WHEN cbf.ownerId = :userId THEN true ELSE false END) as isOwner
        FROM CommunityBoardFile cbf
        WHERE cbf.entryType = :entryType and cbf.entryId = :entryId and cbf.type = :type
        order by cbf.createdDate desc
    """)
    Page<IResponseCommunityBoardFile> findAllByEntryTypeAndEntryIdAndType(
        CommunityBoardEntryTypeEnum entryType,
        UUID userId,
        UUID entryId,
        CommunityBoardFileTypeEnum type,
        Pageable pageable
    );
}
