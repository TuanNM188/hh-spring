package com.formos.huub.service.advisementcategory;

import com.formos.huub.domain.entity.AdvisementCategory;
import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.advisementcategory.RequestAdvisementCategory;
import com.formos.huub.domain.request.advisementcategory.RequestCreateAdvisementCategory;
import com.formos.huub.domain.response.advisementcategory.IResponseAdvisementCategory;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.repository.AdvisementCategoryRepository;
import com.formos.huub.repository.TechnicalAdvisorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvisementCategoryService extends BaseService {

    AdvisementCategoryRepository advisementCategoryRepository;
    TechnicalAdvisorRepository technicalAdvisorRepository;

    /**
     * Get All Advisement Category By technicalAdvisorId
     *
     * @param technicalAdvisorId UUID
     * @return List<IResponseAdvisementCategory>
     */
    public List<IResponseAdvisementCategory> getAllAdvisementCategoryByTechnicalAdvisorId(UUID technicalAdvisorId) {
        TechnicalAdvisor technicalAdvisor = this.checkExistTechnicalAdvisor(technicalAdvisorId);
        return advisementCategoryRepository.getAllAdvisementCategoryByUserId(technicalAdvisor.getUser().getId());
    }

    /**
     * Update User Advisement Category
     *
     * @param technicalAdvisorId UUID
     * @param request List<RequestAdvisementCategory>
     */
    public void updateUserAdvisementCategory(UUID technicalAdvisorId, List<RequestAdvisementCategory> request) {
        TechnicalAdvisor technicalAdvisor = this.checkExistTechnicalAdvisor(technicalAdvisorId);
        User user = technicalAdvisor.getUser();
        List<AdvisementCategory> advisementCategories = new ArrayList<>();
        advisementCategoryRepository.deleteAllAdvisementCategoryByUserId(user.getId());
        for (RequestAdvisementCategory category : request) {
            advisementCategories.addAll(this.createAdvisementCategories(category, user));
        }
        advisementCategoryRepository.saveAll(advisementCategories);
    }

    /**
     * Check Exist TA
     *
     * @param technicalAdvisorId UUID
     */
    private TechnicalAdvisor checkExistTechnicalAdvisor(UUID technicalAdvisorId) {
        Optional<TechnicalAdvisor> technicalAdvisorOpt = technicalAdvisorRepository.findById(technicalAdvisorId);
        if (technicalAdvisorOpt.isEmpty()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Advisor"));
        }
        return technicalAdvisorOpt.get();
    }

    /**
     * Create Advisement Category
     *
     * @param category RequestAdvisementCategory
     * @param user User
     */
    private List<AdvisementCategory> createAdvisementCategories(RequestAdvisementCategory category, User user) {
        List<AdvisementCategory> advisementCategories = new ArrayList<>();
        for (String serviceId: category.getServiceIds()) {
            var createAdvisementCategory = RequestCreateAdvisementCategory
                .builder()
                .user(user)
                .categoryId(category.getId())
                .serviceId(UUID.fromString(serviceId))
                .yearsOfExperience(category.getYearsOfExperience());
            var advisementCategory = createAdvisementCategory.build().toAdvisementCategory();
            advisementCategories.add(advisementCategory);
        }
        return advisementCategories;
    }
}
