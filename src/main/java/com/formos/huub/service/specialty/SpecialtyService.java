package com.formos.huub.service.specialty;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.request.specialty.*;
import com.formos.huub.domain.response.specialty.IResponseSpecialtyUser;
import com.formos.huub.domain.response.specialty.ResponseSpecialty;
import com.formos.huub.domain.response.specialty.ResponseSpecialtyArea;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.mapper.specialty.SpecialtyAreaMapper;
import com.formos.huub.mapper.specialty.SpecialtyMapper;
import com.formos.huub.repository.SpecialtyAreaRepository;
import com.formos.huub.repository.SpecialtyRepository;
import com.formos.huub.repository.SpecialtyUserRepository;
import com.formos.huub.repository.TechnicalAdvisorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpecialtyService {

    SpecialtyRepository specialtyRepository;

    SpecialtyAreaRepository specialtyAreaRepository;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    SpecialtyMapper specialtyMapper;

    SpecialtyAreaMapper specialtyAreaMapper;

    SpecialtyUserRepository specialtyUserRepository;


    /**
     * Get all specialties in system
     */
    public Object getAll(String isArea) {
        if (Boolean.TRUE.equals(Boolean.parseBoolean(isArea))) {
            return specialtyRepository.getAllSpecialtyAndArea();
        }
        return specialtyMapper.toListResponse(specialtyRepository.findAll());
    }

    /**
     * save Specialty For Technical Advisor
     *
     * @param technicalAdvisorId UUID
     * @param request            List<RequestUpdateSpecialtyForTA>
     */
    public void saveSpecialtyForTechnicalAdvisor(UUID technicalAdvisorId, List<RequestSpecialtyForTA> request) {
        TechnicalAdvisor technicalAdvisor = getTechnicalAdvisor(technicalAdvisorId);
        User user = technicalAdvisor.getUser();
        List<SpecialtyUser> specialtyUsers = new ArrayList<>();
        specialtyUserRepository.deleteAllByUserId(user.getId());
        for (RequestSpecialtyForTA specialty : request) {
            specialtyUsers.addAll(buildSpecialtyUser(specialty, user));
        }
        specialtyUserRepository.saveAll(specialtyUsers);
    }

    /**
     * get All By User Id
     *
     * @param technicalAdvisorId UUID
     * @return List<IResponseSpecialtyUser>
     */
    public List<IResponseSpecialtyUser> getAllByUserId(UUID technicalAdvisorId) {
        var technicalAdvisor = getTechnicalAdvisor(technicalAdvisorId);
        return specialtyUserRepository.getAllByUserId(technicalAdvisor.getUser().getId());
    }


    /**
     * create Specialty
     *
     * @param request RequestCreateSpecialty Object request
     * @return Object response
     */
    public ResponseSpecialty createSpecialty(RequestCreateSpecialty request) {

        var isExistName = specialtyRepository.existsByName(request.getName());
        if (isExistName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Specialty"));
        }
        var specialty = specialtyMapper.toEntity(request);
        specialty = specialtyRepository.save(specialty);
        return specialtyMapper.toResponse(specialty);
    }


    /**
     * update Specialty
     *
     * @param specialtyId UUID
     * @param request     RequestUpdateSpecialty
     * @return ResponseSpecialty
     */
    public ResponseSpecialty updateSpecialty(UUID specialtyId, RequestUpdateSpecialty request) {
        var specialty = getSpecialty(specialtyId);
        var isExistCategoryName = specialtyRepository.existsByNameNotEqualId(request.getName(), specialtyId);
        if (isExistCategoryName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Specialty"));
        }
        specialtyMapper.partialEntity(specialty, request);
        var service = specialtyRepository.save(specialty);
        return specialtyMapper.toResponse(service);
    }


    /**
     * delete Specialty
     *
     * @param specialtyId UUID
     */
    public void deleteSpecialty(UUID specialtyId) {
        Boolean isCurrentlyInUse = specialtyUserRepository.existBySpecialtyId(specialtyId);
        if (isCurrentlyInUse) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Specialty"));
        }
        specialtyRepository.deleteById(specialtyId);
    }

    /**
     * get Specialty by id
     *
     * @param specialtyId UUID
     * @return Specialty Object
     */
    private Specialty getSpecialty(UUID specialtyId) {
        return specialtyRepository
            .findById(specialtyId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Specialty")));
    }


    /**
     * Get all by specialty id
     *
     * @param specialtyId UUID
     * @return specialtyId<ResponseSpecialtyArea>
     */
    public List<ResponseSpecialtyArea> getAllBySpecialtyId(UUID specialtyId) {
        List<SpecialtyArea> specialtyAreas = specialtyAreaRepository.findAllBySpecialtyId(specialtyId).stream().toList();
        return specialtyAreaMapper.toListResponse(specialtyAreas);
    }


    /**
     * create Specialty Area
     *
     * @param specialtyId UUID
     * @param request     RequestCreateSpecialtyArea
     * @return ResponseSpecialtyArea
     */
    public ResponseSpecialtyArea createSpecialtyArea(UUID specialtyId, RequestCreateSpecialtyArea request) {
        Specialty specialty = getSpecialty(specialtyId);
        Boolean isExistName = specialtyAreaRepository.existByNameAndSpecialtyId(request.getName(), specialtyId);
        if (isExistName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Specialty Area"));
        }
        SpecialtyArea specialtyArea = specialtyAreaMapper.toEntity(request);
        specialtyArea.setSpecialty(specialty);
        specialtyArea = specialtyAreaRepository.save(specialtyArea);
        return specialtyAreaMapper.toResponse(specialtyArea);
    }


    /**
     * update Specialty Area
     *
     * @param specialtyAreaId UUID
     * @param request         RequestUpdateSpecialtyArea
     * @return ResponseSpecialtyArea
     */
    public ResponseSpecialtyArea updateSpecialtyArea(UUID specialtyAreaId, RequestUpdateSpecialtyArea request) {
        SpecialtyArea specialtyArea = specialtyAreaRepository
            .findById(specialtyAreaId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Specialty Area")));
        Specialty specialty = specialtyArea.getSpecialty();
        var isExistName = specialtyAreaRepository.existByNameAndNotEqualId(request.getName(), specialtyAreaId, specialty.getId());
        if (isExistName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Specialty Area"));
        }
        BeanUtils.copyProperties(request, specialtyArea);
        specialtyArea = specialtyAreaRepository.save(specialtyArea);
        return specialtyAreaMapper.toResponse(specialtyArea);
    }

    /**
     * Delete Specialty Area
     *
     * @param specialtyAreaId UUID
     */
    public void deleteSpecialtyArea(UUID specialtyAreaId) {
        Boolean isCurrentlyInUse = specialtyUserRepository.existBySpecialtyAreaId(specialtyAreaId);
        if (isCurrentlyInUse) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0036, "Specialty Area"));
        }
        specialtyAreaRepository.deleteById(specialtyAreaId);
    }


    /**
     * build Specialty User
     *
     * @param request RequestUpdateSpecialtyForTA
     * @param user    User
     * @return List<SpecialtyUser>
     */
    private List<SpecialtyUser> buildSpecialtyUser(RequestSpecialtyForTA request, User user) {
        if (ObjectUtils.isEmpty(request.getSpecialtyAreaIds())) {
            return List.of();
        }
        return request.getSpecialtyAreaIds().stream().map(ele -> SpecialtyUser.builder()
            .specialtyAreaId(ele)
            .specialtyId(request.getId())
            .user(user)
            .yearsOfExperience(request.getYearsOfExperience())
            .build()).toList();
    }


    private TechnicalAdvisor getTechnicalAdvisor(UUID technicalAdvisorId) {
        return technicalAdvisorRepository
            .findById(technicalAdvisorId)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "Technical Advisor")));
    }
}
