package com.formos.huub.service.referralmessage;

import com.formos.huub.config.Constants;
import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.BusinessOwner;
import com.formos.huub.domain.entity.ReferralMessage;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.request.directmessage.RequestSearchReferralMessage;
import com.formos.huub.domain.request.project.RequestSearchProject;
import com.formos.huub.domain.response.directmessage.ResponseDetailReferralMessage;
import com.formos.huub.domain.response.directmessage.ResponseUserBasic;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.referralmessage.ReferralMessageMapper;
import com.formos.huub.repository.BusinessOwnerRepository;
import com.formos.huub.repository.LearningLibraryRepository;
import com.formos.huub.repository.ReferralMessageRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.service.applicationmanagement.ApplicationManagementService;
import com.formos.huub.service.learninglibrary.LearningLibraryService;
import com.formos.huub.service.learninglibraryregistration.LearningLibraryRegistrationService;
import com.sendgrid.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReferralMessageService {


    ReferralMessageRepository referralMessageRepository;

    ReferralMessageMapper referralMessageMapper;

    UserRepository userRepository;

    BusinessOwnerRepository businessOwnerRepository;

    LearningLibraryRegistrationService learningLibraryRegistrationService;


    /**
     * search Appointments by term and condition
     *
     * @param request RequestSearchAppointment
     * @return Map<String, Object> appointment
     */
    public Map<String, Object> searchReferralMessages(RequestSearchReferralMessage request, String timezone) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "rm.sendAt,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));

        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put(BusinessConstant.TIMEZONE_KEY, timezone);
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));
        request.setRegexHtml(Constants.REMOVE_HTML_REGEX);
        var data = referralMessageRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    public ResponseDetailReferralMessage getDetail(UUID id) {
        var referralMessage = referralMessageRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Referral Message")));

        var response = referralMessageMapper.toResponse(referralMessage);

        Optional.ofNullable(referralMessage.getBusinessOwner())
            .map(businessOwner -> {
                var businessOwnerUser = userRepository.findById(businessOwner.getUser().getId())
                    .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Business Owner")));

                return buildUserResponse(businessOwnerUser, businessOwner.getId(),
                    learningLibraryRegistrationService.getBusinessName(businessOwnerUser.getId()));
            })
            .ifPresent(response::setBusinessOwner);

        Optional.ofNullable(referralMessage.getPrimaryUser())
            .map(primaryUser -> buildUserResponse(primaryUser, null, null))
            .ifPresent(response::setPrimaryUser);

        return response;
    }

    private ResponseUserBasic buildUserResponse(User user, UUID businessOwnerId, String businessName) {
        var userResponse = new ResponseUserBasic();
        userResponse.setId(user.getId());
        userResponse.setFullName(user.getNormalizedFullName());
        userResponse.setImageUrl(user.getImageUrl());

        if (businessOwnerId != null) {
            userResponse.setBusinessOwnerId(businessOwnerId);
        }
        if (businessName != null) {
            userResponse.setBusinessName(businessName);
        }

        return userResponse;
    }

}
