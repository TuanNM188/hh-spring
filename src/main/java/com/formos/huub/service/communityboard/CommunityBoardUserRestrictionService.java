package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardUserRestriction;
import com.formos.huub.domain.enums.CommunityBoardRestrictionTypeEnum;
import com.formos.huub.domain.request.communityboarduserrestrict.RequestAddRestrictMember;
import com.formos.huub.domain.request.communityboarduserrestrict.RequestRemoveRestrictMember;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.repository.CommunityBoardUserRestrictionRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.service.pushnotification.PushNotificationService;
import java.time.Instant;
import java.util.Objects;
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
public class CommunityBoardUserRestrictionService extends BaseService {

    CommunityBoardUserRestrictionRepository restrictionRepository;
    UserRepository userRepository;
    PushNotificationService pushNotificationService;

    public UUID addRestrictMember(RequestAddRestrictMember request) {
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        if (Objects.isNull(portalId)) {
            portalId = PortalContextHolder.getPortalId();
        }
        var userId = UUIDUtils.convertToUUID(request.getUserId());
        var user = userRepository
            .findById(userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
        if (restrictionRepository.existsByPortalIdAndUserId(portalId, user.getId(), CommunityBoardRestrictionTypeEnum.POST)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0064));
        }

        var result = CommunityBoardUserRestriction.builder()
            .userId(user.getId())
            .portalId(portalId)
            .restrictionType(CommunityBoardRestrictionTypeEnum.POST)
            .startDate(Instant.now())
            .build();
        restrictionRepository.save(result);

        // send notify for member restrict
        log.info("Sending restrict notification to user: {}", user.getId());
        pushNotificationService.sendNotifyWhenRestrictMemberPost(user);
        return result.getId();
    }

    public void removeRestrictMember(RequestRemoveRestrictMember request) {
        var portalId = UUIDUtils.toUUID(request.getPortalId());
        if (Objects.isNull(portalId)) {
            portalId = PortalContextHolder.getPortalId();
        }
        var userId = UUIDUtils.convertToUUID(request.getUserId());
        var userRestrict = restrictionRepository
            .findByPortalIdAndUserId(portalId, userId, CommunityBoardRestrictionTypeEnum.POST)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0065)));
        restrictionRepository.delete(userRestrict);
    }
}
