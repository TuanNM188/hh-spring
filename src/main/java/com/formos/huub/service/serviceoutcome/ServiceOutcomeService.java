package com.formos.huub.service.serviceoutcome;

import com.formos.huub.domain.entity.ServiceOffered;
import com.formos.huub.domain.entity.ServiceOutcome;
import com.formos.huub.domain.request.serviceoutcome.RequestCreateServiceOutcome;
import com.formos.huub.domain.request.serviceoutcome.RequestUpdateServiceOutcome;
import com.formos.huub.domain.response.serviceoutcome.ResponseServiceOutcome;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.serviceoutcome.ServiceOutcomeMapper;
import com.formos.huub.repository.ServiceOfferedRepository;
import com.formos.huub.repository.ServiceOutcomeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServiceOutcomeService extends BaseService {

    ServiceOutcomeRepository serviceOutcomeRepository;
    ServiceOfferedRepository serviceOfferedRepository;
    ServiceOutcomeMapper serviceOutcomeMapper;

    /**
     * Get all by service offer id
     *
     * @param serviceOfferedId UUID
     * @return List<ResponseServiceOutcome>
     */
    public List<ResponseServiceOutcome> getAllByServiceOfferedId(UUID serviceOfferedId) {
        List<ServiceOutcome> serviceOutcomes = serviceOutcomeRepository.getAllByServiceOfferedId(serviceOfferedId).stream().toList();
        return serviceOutcomeMapper.toListResponse(serviceOutcomes);
    }

    /**
     * Create Service Outcome
     *
     * @param serviceOfferedId UUID
     * @param request RequestCreateServiceOutcome
     * @return ResponseServiceOutcome
     */
    public ResponseServiceOutcome createServiceOutcome(UUID serviceOfferedId, RequestCreateServiceOutcome request) {
        ServiceOffered serviceOffered = serviceOfferedRepository
            .findById(serviceOfferedId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Service Offered")));

        Boolean isExistServiceName = serviceOutcomeRepository.existByNameAndServiceOfferedId(request.getName(), serviceOfferedId);
        if (isExistServiceName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "service"));
        }
        ServiceOutcome serviceOutcome = new ServiceOutcome();
        BeanUtils.copyProperties(request, serviceOutcome);
        serviceOutcome.setServiceOffered(serviceOffered);
        ServiceOutcome service = serviceOutcomeRepository.save(serviceOutcome);
        return serviceOutcomeMapper.toResponse(service);
    }

    /**
     * Update Service Outcome
     *
     * @param serviceId UUID
     * @param request RequestCreateServiceOutcome
     * @return ResponseServiceOutcome
     */
    public ResponseServiceOutcome updateServiceOutcome(UUID serviceId, RequestUpdateServiceOutcome request) {
        ServiceOutcome serviceOutcome = serviceOutcomeRepository
            .findById(serviceId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Service Outcome")));
        ServiceOffered serviceOffered = serviceOutcome.getServiceOffered();

        var isExistServiceName = serviceOutcomeRepository.existByNameAndNotEqualId(request.getName(), serviceId, serviceOffered.getId());
        if (isExistServiceName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "service"));
        }
        BeanUtils.copyProperties(request, serviceOutcome);
        ServiceOutcome service = serviceOutcomeRepository.save(serviceOutcome);
        return serviceOutcomeMapper.toResponse(service);
    }

    /**
     * Delete Service Outcome
     *
     * @param serviceId UUID
     */
    public void deleteServiceOutcome(UUID serviceId) {
        ServiceOutcome serviceOutcome = serviceOutcomeRepository
            .findById(serviceId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Service Outcome")));
        serviceOutcomeRepository.delete(serviceOutcome);
    }
}
