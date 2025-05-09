package com.formos.huub.mapper.categories;

import com.formos.huub.domain.entity.Category;
import com.formos.huub.domain.response.categories.IResponseCategory;
import com.formos.huub.domain.response.categories.ResponseCategories;
import com.formos.huub.domain.response.funding.ResponseFundingCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    ResponseCategories toResponse(Category category);
    List<ResponseCategories> toListResponse(List<Category> categories);
    List<ResponseCategories> toListResponseFromInterface(List<IResponseCategory> categories);

    Category stringToEntity(String id);
}
