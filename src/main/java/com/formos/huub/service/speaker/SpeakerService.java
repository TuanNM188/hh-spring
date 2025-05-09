package com.formos.huub.service.speaker;

import com.formos.huub.domain.entity.Speaker;
import com.formos.huub.domain.request.speaker.RequestCreateSpeaker;
import com.formos.huub.domain.request.speaker.RequestUpdateSpeaker;
import com.formos.huub.domain.response.speaker.ResponseSpeakerDetail;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.mapper.speaker.SpeakerMapper;
import com.formos.huub.repository.SpeakerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpeakerService {

    SpeakerRepository speakerRepository;

    SpeakerMapper speakerMapper;

    /**
     * Get all speaker
     *
     * @return List<ResponseSpeakerDetail>
     */
    public List<ResponseSpeakerDetail> getAll() {
        List<Speaker> speakers = speakerRepository.findAll();
        return speakerMapper.toListResponse(speakers);
    }

    /**
     * Create speaker
     *
     * @param request RequestCreateSpeaker
     * @return ResponseSpeakerDetail
     */
    public ResponseSpeakerDetail createSpeaker(RequestCreateSpeaker request) {
        Speaker speaker = speakerMapper.toEntity(request);
        speakerRepository.save(speaker);
        return speakerMapper.toResponse(speaker);
    }

    /**
     * Update speaker
     *
     * @param id      UUID
     * @param request RequestUpdateSpeaker
     * @return ResponseSpeakerDetail
     */
    public ResponseSpeakerDetail updateSpeaker(UUID id, RequestUpdateSpeaker request) {
        Speaker speaker = getSpeaker(id);
        speakerMapper.partialUpdate(speaker, request);
        speaker = speakerRepository.save(speaker);
        return speakerMapper.toResponse(speaker);
    }

    /**
     * Get all by learning library id
     *
     * @param learningLibraryId UUID
     * @return List<ResponseSpeakerDetail>
     */
    public List<ResponseSpeakerDetail> getAllByLearningLibraryId(UUID learningLibraryId) {
        List<Speaker> speakers = speakerRepository.getSpeakerByLearningLibraryId(learningLibraryId);
        return speakerMapper.toListResponse(speakers);
    }

    /**
     * Get speaker
     *
     * @param id UUID
     * @return Speaker
     */
    private Speaker getSpeaker(UUID id) {
        return speakerRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0010, "speaker")));
    }

}
