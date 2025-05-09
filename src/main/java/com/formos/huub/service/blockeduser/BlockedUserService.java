package com.formos.huub.service.blockeduser;

import com.formos.huub.domain.enums.ConversationTypeEnum;
import com.formos.huub.domain.response.blockeduser.IResponseBlockedUser;

import com.formos.huub.repository.BlockedUserRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class BlockedUserService {

    BlockedUserRepository blockedUserRepository;

    UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<IResponseBlockedUser> getBlockedUserByCurrentUser() {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        return blockedUserRepository.findBockedUserByBlockerId(currentUser.getId());
    }

}
