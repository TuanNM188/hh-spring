package com.formos.huub.service.fundingservice;

import com.formos.huub.domain.entity.Funding;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.entity.embedkey.PortalFundingEmbedKey;
import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.request.portals.RequestCreatePortalFunding;
import com.formos.huub.domain.request.portals.RequestPortalFundingAbout;
import com.formos.huub.domain.request.portals.RequestSearchPortalFunding;
import com.formos.huub.domain.request.portals.RequestUpdatePortalFunding;
import com.formos.huub.domain.response.portals.ResponsePortalFundingDetail;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.portals.PortalFundingMapper;
import com.formos.huub.repository.FundingRepository;
import com.formos.huub.repository.PortalFeatureRepository;
import com.formos.huub.repository.PortalFundingRepository;
import com.formos.huub.repository.PortalRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PortalFundingService extends BaseService {

    FundingRepository fundingRepository;

    PortalFundingMapper portalFundingMapper;

    PortalRepository portalRepository;

    PortalFeatureRepository portalFeatureRepository;

    PortalFundingRepository portalFundingRepository;

    /**
     * search Portal Funding
     *
     * @param request SearchPortalsRequest
     * @return Map<String, Object>
     */
    public Map<String, Object> searchPortalFunding(RequestSearchPortalFunding request) {

        checkPermissionPortal(request.getPortalId());
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "f.createdDate,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        var data = fundingRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * delete Portal Funding
     *
     * @param fundingId UUID
     */
    public void deletePortalFunding(UUID portalId, UUID fundingId) {
        checkPermissionPortal(portalId);
        var portal = getPortal(portalId);
        var funding = fundingRepository.findById(fundingId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Funding")));
        var portals = funding.getPortals();
        if (!ObjectUtils.isEmpty(portals) && portals.size() > 1) {
            var id = PortalFundingEmbedKey.builder()
                .portal(portal)
                .funding(funding).build();
            portalFundingRepository.deleteById(id);
            return;
        }
        fundingRepository.deleteById(fundingId);
    }

    /**
     * Create portal funding
     *
     * @param request  RequestCreatePortalFunding
     * @return UUID
     */
    public UUID createPortalFunding( RequestCreatePortalFunding request) {
        Set<Portal> portals = getPortals(request.getPortalFundingAbout().getPortalIds());
        if (fundingRepository.existsByTitle(request.getPortalFundingAbout().getTitle())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Portal Funding"));
        }
        Funding funding = portalFundingMapper.toEntity(request);
        funding.setPortals(portals);
        funding = fundingRepository.save(funding);
        return funding.getId();
    }

    /**
     * Find portal funding by id
     *
     * @param fundingId UUID
     * @return ResponsePortalFundingDetail
     */
    public ResponsePortalFundingDetail findPortalFundingById(UUID fundingId) {
        Funding funding = getPortalFunding(fundingId);
        ResponsePortalFundingDetail response = portalFundingMapper.toResponseDetail(funding);
        List<UUID> portalIds = funding.getPortals().stream().map(Portal::getId).toList();
        response.getPortalFundingAbout().setPortalIds(portalIds);
        return response;
    }

    /**
     * Update portal funding
     *
     * @param fundingId UUID
     * @param request   RequestUpdatePortalFunding
     */
    public void updatePortalFunding( UUID fundingId, RequestUpdatePortalFunding request) {
        var portals = getPortals(request.getPortalFundingAbout().getPortalIds());
        RequestPortalFundingAbout portalFundingAbout = request.getPortalFundingAbout();
        Funding funding = getPortalFunding(fundingId);
        String newTitle = portalFundingAbout.getTitle();
        if (!funding.getTitle().equals(newTitle)
            && fundingRepository.existsByTitle(newTitle)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Portal Funding"));
        }
        portalFundingMapper.partialUpdate(funding, request);
        funding.setPortals(portals);
        fundingRepository.save(funding);
    }

    /**
     * Get portal funding
     *
     * @param fundingId UUID
     * @return Funding
     */
    private Funding getPortalFunding(UUID fundingId) {
        return fundingRepository.findById(fundingId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Funding")));
    }

    /**
     * Get portal
     *
     * @param id UUID
     * @return Portal
     */
    private Portal getPortal(UUID id) {
        return portalRepository.findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
    }

    private Set<Portal> getPortals(List<UUID> portalIds) {
        return new HashSet<>(portalRepository.findAllById(portalIds));
    }

    /**
     * check Permission Portal
     *
     * @param portalId UUID
     */
    private void checkPermissionPortal(UUID portalId) {
        if (ObjectUtils.isEmpty(portalId)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "PortalId"));
        }
        var portalFeatureOpt = portalFeatureRepository.findById_Feature_FeatureCodeAndId_Portal_Id(FeatureCodeEnum.FUNDING_DIRECTORY, portalId);
        if (portalFeatureOpt.isEmpty() || !portalFeatureOpt.get().getIsActive()) {
            throw new NotFoundException(MessageHelper.getMessage(Message.Keys.E0006, "Portal Funding"));
        }
    }

}
