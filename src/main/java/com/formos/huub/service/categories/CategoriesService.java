package com.formos.huub.service.categories;

import com.formos.huub.domain.entity.Category;
import com.formos.huub.domain.request.categories.RequestCreateCategory;
import com.formos.huub.domain.request.categories.RequestUpdateCategory;
import com.formos.huub.domain.response.categories.IResponseCategory;
import com.formos.huub.domain.response.categories.ResponseCategories;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.categories.CategoryMapper;
import com.formos.huub.repository.AdvisementCategoryRepository;
import com.formos.huub.repository.CategoryRepository;

import com.formos.huub.repository.FundingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CategoriesService {

    CategoryRepository categoryRepository;

    CategoryMapper categoryMapper;

    AdvisementCategoryRepository advisementCategoryRepository;


    /**
     * Get all category
     */
    public List<ResponseCategories> getAll() {
        var result = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().toList();
        return categoryMapper.toListResponse(result);
    }

    /**
     * create category
     *
     * @param request RequestCreateCategory
     */
    public ResponseCategories createCategories(RequestCreateCategory request) {

        var isExistCategoryName = categoryRepository.existsByName(request.getName());
        if (isExistCategoryName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "category"));
        }
        var category = new Category();
        BeanUtils.copyProperties(request, category);
        var categories = categoryRepository.save(category);
        return categoryMapper.toResponse(categories);
    }

    /**
     * update Category
     *
     * @param categoryId UUID
     * @param request    RequestUpdateCategory
     */
    public ResponseCategories updateCategories(UUID categoryId, RequestUpdateCategory request) {
        var category = categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "category")));
        var isExistCategoryName = categoryRepository.existsByNameAndNotEqualId(request.getName(), categoryId);
        if (isExistCategoryName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "category"));
        }
        BeanUtils.copyProperties(request, category);
        var categories = categoryRepository.save(category);
        return categoryMapper.toResponse(categories);
    }

    /**
     * delete Category
     *
     * @param categoryId UUID categoryId
     */
    public void deleteCategory(UUID categoryId) {
        var categories = categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "category")));
        boolean isCurrentlyInUse = advisementCategoryRepository.existByCategoryId(categoryId) ;
        if (isCurrentlyInUse) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Category"));
        }
        categoryRepository.delete(categories);
    }

    /**
     * get all categories and services
     *
     * @return List<ResponseCategories>
     */
    public List<ResponseCategories> getAllCategoriesAndServices() {
        List<IResponseCategory> categories = categoryRepository.getAllCategoriesAndServices();
        return categoryMapper.toListResponseFromInterface(categories);
    }
}
