package com.formos.huub.service.communitypartner;

import com.formos.huub.domain.entity.CommunityPartner;
import com.formos.huub.domain.entity.ProgramTerm;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.request.communityresource.RequestSearchCommunityResource;
import com.formos.huub.domain.response.communityresource.ResponseCommunityResourceMember;
import com.formos.huub.domain.response.communityresource.ResponseDetailCommunityResource;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.communitypartner.CommunityPartnerMapper;
import com.formos.huub.repository.CommunityPartnerRepository;
import com.formos.huub.repository.TechnicalAdvisorRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CommunityResourceService {

    CommunityPartnerRepository communityPartnerRepository;

    CommunityPartnerMapper communityPartnerMapper;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    UserRepository userRepository;


    /**
     * search Community Resources by portal ID and conditions
     *
     * @param request  RequestSearchCommunityResource
     * @param pageable Pageable
     * @return response community partner in portal
     */
    public Map<String, Object> searchCommunityResources(RequestSearchCommunityResource request, Pageable pageable) {
        var portalContext = PortalContextHolder.getContext();
        if (Objects.nonNull(portalContext)) {
            request.setPortalId(portalContext.getPortalId());
        }
        var data = communityPartnerRepository.searchCommunityResource(request, pageable);
        return PageUtils.toPage(data);
    }

    /**
     * get Detail Community Resource
     *
     * @param id community resource id
     * @return ResponseDetailCommunityResource object response
     */
    public ResponseDetailCommunityResource getDetailCommunityResource(UUID id) {
        // Fetch and map the community resource
        var communityResource = getCommunityPartner(id);
        var response = communityPartnerMapper.toResponseDetailCommunityResource(communityResource);
        // Create a list for team members and add the community partner user
        List<ResponseCommunityResourceMember> teamMembers = getTeamMembersInCommunityResource(communityResource);
        response.setTeamMembers(teamMembers);

        User primaryContact = getCommunityPartnerPrimaryContactId(id);
        if (Objects.nonNull(primaryContact)){
            response.setPrimaryContactId(primaryContact.getId());
            response.setPrimaryContactName(primaryContact.getNormalizedFullName());
        }
        return response;
    }

    private List<ResponseCommunityResourceMember> getTeamMembersInCommunityResource(CommunityPartner communityPartner) {
        var teamMembers = new ArrayList<ResponseCommunityResourceMember>();
        // Add technical advisors, community partner to the team members list
        technicalAdvisorRepository.getTechnicalAdvisorCommunityPartner(communityPartner.getId(), UserStatusEnum.ACTIVE)
            .forEach(advisor -> {
                var member = new ResponseCommunityResourceMember();
                BeanUtils.copyProperties(advisor, member);
                teamMembers.add(member);
            });
        return teamMembers.stream().sorted(Comparator.comparing(ResponseCommunityResourceMember::getRole))
            .toList();
    }

    /**
     * Get Community Partner Primary Contact Id
     *
     * @param communityPartnerId UUID
     * @return UUID
     */
    public User getCommunityPartnerPrimaryContactId(UUID communityPartnerId) {
        return userRepository.getCommunityPartnerPrimaryById(communityPartnerId)
            .orElse(null);
    }

    private CommunityPartner getCommunityPartner(UUID communityPartnerId) {
        return communityPartnerRepository.findById(communityPartnerId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Community Resource")));
    }
}
