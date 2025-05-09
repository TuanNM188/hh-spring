package com.formos.huub.service.funder;

import com.formos.huub.domain.entity.Funder;
import com.formos.huub.domain.request.funder.RequestCreateFunder;
import com.formos.huub.domain.request.funder.RequestUpdateFunder;
import com.formos.huub.domain.response.funder.ResponseFunder;
import com.formos.huub.domain.response.funder.ResponseFunderDetail;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.funder.FunderMapper;
import com.formos.huub.repository.FunderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FunderService {

    FunderRepository funderRepository;

    FunderMapper funderMapper;

    /**
     * Get all funder
     *
     * @return List<ResponseFunder>
     */
    public List<ResponseFunder> getAll() {
        List<Funder> funders = funderRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream().toList();
        return funderMapper.toListResponse(funders);
    }

    /**
     * Create funder
     *
     * @param request RequestCreateFunder
     * @return ResponseFunderDetail
     */
    public ResponseFunderDetail createFunder(RequestCreateFunder request) {
        Boolean isExistsFunderName = funderRepository.existsByName(request.getName());
        if (isExistsFunderName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "funder"));
        }
        Funder funder = funderMapper.toEntity(request);
        funderRepository.save(funder);
        return funderMapper.toResponseDetail(funder);
    }

    /**
     * Update funder
     *
     * @param id UUID
     * @param request RequestUpdateFunder
     * @return ResponseFunderDetail
     */
    public ResponseFunderDetail updateFunder(UUID id, RequestUpdateFunder request) {
        Funder funder = getFunder(id);
        Boolean isExistsFunderName = funderRepository.existsByNameAndNotEqualId(request.getName(), id);
        if (isExistsFunderName) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "funder"));
        }
        funderMapper.partialUpdate(funder, request);
        funder = funderRepository.save(funder);
        return funderMapper.toResponseDetail(funder);
    }

    /**
     * Find funder by id
     * @param id UUID
     * @return ResponseFunderDetail
     */
    public ResponseFunderDetail findFunderById(UUID id) {
        Funder funder = getFunder(id);
        return funderMapper.toResponseDetail(funder);
    }

    /**
     * Get funder
     *
     * @param id UUID
     * @return Funder
     */
    private Funder getFunder(UUID id) {
        return funderRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "funder")));
    }
}
