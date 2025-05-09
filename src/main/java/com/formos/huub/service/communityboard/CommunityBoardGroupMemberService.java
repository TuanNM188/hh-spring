package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardGroupMember;
import com.formos.huub.repository.CommunityBoardGroupMemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardGroupMemberService {

    CommunityBoardGroupMemberRepository groupMemberRepository;

    public List<CommunityBoardGroupMember> getAllMembers() {
        return groupMemberRepository.findAll();
    }

    public Optional<CommunityBoardGroupMember> getMemberById(UUID id) {
        return groupMemberRepository.findById(id);
    }

    public CommunityBoardGroupMember saveMember(CommunityBoardGroupMember member) {
        return groupMemberRepository.save(member);
    }

    public void deleteMember(UUID id) {
        groupMemberRepository.deleteById(id);
    }
}
