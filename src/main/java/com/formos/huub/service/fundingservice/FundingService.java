package com.formos.huub.service.fundingservice;

import com.formos.huub.config.Constants;
import com.formos.huub.domain.entity.Category;
import com.formos.huub.domain.entity.Funding;
import com.formos.huub.domain.entity.FundingSubmitted;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.FavoriteTypeEnum;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.request.funding.RequestSearchFunding;
import com.formos.huub.domain.response.funding.*;
import com.formos.huub.domain.response.learninglibrary.ResponseRecommendCourse;
import com.formos.huub.domain.response.portals.ResponseNewlyFeature;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.funding.FundingMapper;
import com.formos.huub.repository.FundingRepository;
import com.formos.huub.repository.FundingSubmittedRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.favorite.FavoriteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static com.formos.huub.domain.constant.BusinessConstant.NUMBER_0;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FundingService extends BaseService {

    FundingRepository fundingRepository;

    UserRepository userRepository;

    FundingMapper fundingMapper;

    FavoriteService favoriteService;

    FundingSubmittedRepository fundingSubmittedRepository;

    /**
     * search Funding By Condition
     *
     * @param request  RequestSearchFunding
     * @param pageable Pageable
     * @return Map<String, Object>
     */
    public Map<String, Object> searchFundingByConditions(RequestSearchFunding request, Pageable pageable) {
        request.setPortalId(PortalContextHolder.getPortalId());
        var currentUser = getCurrentUser();
        var userId = getCurrentUser() != null ? currentUser.getId() : null;
        request.setUserId(userId);
        if (Objects.nonNull(request.getCurrentDate())) {
            LocalDate currentDate = LocalDate.parse(request.getCurrentDate());
            request.setEndExpiringSoonDate(currentDate.plusDays(14).toString());
            request.setStartRecentlyAdded(currentDate.minusDays(14).toString());
        }
        request.setSearchKeyword(StringUtils.wildcards(request.getSearchKeyword()));
        var data = fundingRepository.searchByConditions(request, Constants.REMOVE_HTML_REGEX, pageable).map(this::mapToResponseFunding);
        return PageUtils.toPage(data);
    }

    /**
     * get Related Funding
     *
     * @param request RequestSearchFunding
     * @return List<IResponseSearchFunding>
     */
    public List<ResponseSearchFunding> getRelatedFunding(RequestSearchFunding request) {
        var currentUser = getCurrentUser();
        request.setUserId(currentUser.getId());
        request.setPortalId(PortalContextHolder.getPortalId());
        if (Objects.isNull(request.getPortalId())){
            UUID portalId = PortalContextHolder.getPortalId();
            request.setPortalId(portalId);
        }
        return fundingRepository.getRelatedFunding(request).stream().map(this::mapToResponseFunding).toList();
    }


    /**
     * getDetailFundingById
     *
     * @param fundingId ID request
     * @return Mapped ResponseDetailFunding Object
     */
    public ResponseDetailFunding getDetailFundingById(UUID fundingId) {
        var funding = getFunding(fundingId);
        return fundingMapper.toResponse(funding);
    }

    /**
     * favorite Funding
     *
     * @param fundingId UUID request ID
     * @param status    StatusEnum request
     */
    public ResponseStatusFunding favoriteFunding(UUID fundingId, StatusEnum status) {
        var currentUser = getCurrentUser();
        favoriteService.favorite(currentUser.getId(), fundingId, FavoriteTypeEnum.FUNDING, status);
        return ResponseStatusFunding.builder()
            .id(fundingId)
            .status(status)
            .build();
    }

    public ResponseStatusFunding userSubmitFunding(UUID fundingId, StatusEnum status) {
        var currentUser = getCurrentUser();
        fundingSubmitted(currentUser.getId(), fundingId, status);
        return ResponseStatusFunding.builder()
            .id(fundingId)
            .status(status)
            .build();
    }

    public ResponseRecommendFunding getRecommendFunding(UUID portalId) {
        Pageable topOne = PageRequest.of(0, 1);
        var latestLearningLibraries = fundingRepository.findLatestByPortal(portalId, topOne);

        if (latestLearningLibraries.isEmpty()) {
            return null;
        }
        var latest = latestLearningLibraries.get(NUMBER_0);
        return toResponseBaseFunding(latest);
    }

    public List<ResponseNewlyFeature> getNewlyFunding(UUID portalId, Integer size) {
        Pageable topOne = PageRequest.of(0, size);
         return fundingRepository.findLatestByPortal(portalId, topOne).stream().map(this::toResponseNewlyFunding).toList();
    }

    private ResponseNewlyFeature toResponseNewlyFunding(Funding funding){
        var response =  new ResponseNewlyFeature();
        BeanUtils.copyProperties(funding, response);
        response.setFundingType(funding.getType());
        return response;
    }

    private ResponseRecommendFunding toResponseBaseFunding(Funding funding){
        var response =  new ResponseRecommendFunding();
        BeanUtils.copyProperties(funding, response);
        return response;
    }

    private void fundingSubmitted(UUID userId, UUID fundingId, StatusEnum status) {
        var fundingSubmitted = getFundingSubmitted(userId, fundingId);
        fundingSubmitted.setStatus(status);
        fundingSubmittedRepository.save(fundingSubmitted);
    }

    private FundingSubmitted getFundingSubmitted(UUID userId, UUID fundingId) {
        return fundingSubmittedRepository.findByUserIdAndFundingId(userId, fundingId).orElse(
            FundingSubmitted.builder().userId(userId)
                .fundingId(fundingId)
                .build()
        );
    }

    /**
     * Maps Funding entity to ResponseSearchFunding DTO.
     *
     * @param funding The funding entity to map.
     * @return Mapped ResponseSearchFunding object.
     */
    private ResponseSearchFunding mapToResponseFunding(IResponseSearchFunding funding) {
        ResponseSearchFunding response = new ResponseSearchFunding();
        BeanUtils.copyProperties(funding, response);
        if (!ObjectUtils.isEmpty(funding.getFundingCategories())) {
            response.setFundingCategories(List.of(funding.getFundingCategories().split(",")));
        }
        return response;
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin).orElse(null);
    }

    private Funding getFunding(UUID id) {
        return fundingRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Funding")));
    }

}
