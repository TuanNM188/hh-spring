package com.formos.huub.service.serviceoffered;

import com.formos.huub.domain.entity.ServiceOffered;
import com.formos.huub.domain.request.serviceoffered.RequestCreateServiceOffered;
import com.formos.huub.domain.request.serviceoffered.RequestUpdateServiceOffered;
import com.formos.huub.domain.response.serviceoffered.ResponseServiceOffered;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.serviceoffered.ServiceOfferedMapper;
import com.formos.huub.repository.AdvisementCategoryRepository;
import com.formos.huub.repository.CategoryRepository;
import com.formos.huub.repository.ServiceOfferedRepository;
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
public class ServiceOfferedService extends BaseService {

    ServiceOfferedRepository serviceOfferedRepository;

    CategoryRepository categoryRepository;

    ServiceOfferedMapper serviceOfferedMapper;

    AdvisementCategoryRepository advisementCategoryRepository;

    /**
     * Get all service by category id
     */
    public List<ResponseServiceOffered> getAllByCategoryId(UUID categoryId) {
        var result = serviceOfferedRepository.getAllByCategoryId(categoryId).stream().toList();
        return serviceOfferedMapper.toListResponse(result);
    }

    /**
     * create Service Offered
     *
     * @param categoryId UUID
     * @param request    RequestCreateCategory
     */
    public ResponseServiceOffered createService(UUID categoryId, RequestCreateServiceOffered request) {
        var categories = categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Category")));

        var isExistSubCategoryName = serviceOfferedRepository.existsByNameAndCategoryId(request.getName(), categoryId);
        if (isExistSubCategoryName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "service"));
        }
        var serviceOffered = new ServiceOffered();
        BeanUtils.copyProperties(request, serviceOffered);
        serviceOffered.setCategory(categories);
        var service = serviceOfferedRepository.save(serviceOffered);
        return serviceOfferedMapper.toResponse(service);
    }


    /**
     * update Service Offered
     *
     * @param serviceId UUID
     * @param request   RequestUpdateCategory
     */
    public ResponseServiceOffered updateService(UUID serviceId, RequestUpdateServiceOffered request) {
        var serviceOffered = serviceOfferedRepository
            .findById(serviceId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Service")));
        var category = serviceOffered.getCategory();
        var isExistCategoryName = serviceOfferedRepository.existsByNameAndNotEqualId(request.getName(), serviceId, category.getId());
        if (isExistCategoryName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "service"));
        }
        BeanUtils.copyProperties(request, serviceOffered);
        var service = serviceOfferedRepository.save(serviceOffered);
        return serviceOfferedMapper.toResponse(service);
    }


    /**
     * delete Service Offered
     *
     * @param serviceId UUID serviceId
     */
    public void deleteServiceOffered(UUID serviceId) {
        var categories = serviceOfferedRepository
            .findById(serviceId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Service Offered")));
        Boolean isCurrentlyInUse = advisementCategoryRepository.existByServiceOfferedId(serviceId);
        if (isCurrentlyInUse) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Service"));
        }
        serviceOfferedRepository.delete(categories);
    }
}
