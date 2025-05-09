package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.speaker.RequestCreateSpeaker;
import com.formos.huub.domain.request.speaker.RequestUpdateSpeaker;
import com.formos.huub.domain.response.speaker.ResponseSpeakerDetail;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.speaker.SpeakerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/speakers")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpeakerController {

    SpeakerService speakerService;

    ResponseSupport responseSupport;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_SPEAKER')")
    public ResponseEntity<ResponseData> getAllSpeaker() {
        List<ResponseSpeakerDetail> speakers = speakerService.getAll();
        return responseSupport.success(ResponseData.builder().data(speakers).build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_SPEAKER')")
    public ResponseEntity<ResponseData> createSpeaker(@RequestBody @Valid RequestCreateSpeaker request) {
        ResponseSpeakerDetail response = speakerService.createSpeaker(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'UPDATE_SPEAKER')")
    public ResponseEntity<ResponseData> updateSpeaker(@PathVariable @UUIDCheck String id,
                                                      @RequestBody @Valid RequestUpdateSpeaker request) {
        ResponseSpeakerDetail response = speakerService.updateSpeaker(UUID.fromString(id), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/learning-library/{learningLibraryId}")
    @PreAuthorize("hasPermission(null, 'GET_SPEAKER_LEARNING_LIBRARY_BY_ID')")
    public ResponseEntity<ResponseData> getAllSpeakerByLearningLibraryId(@PathVariable @UUIDCheck String learningLibraryId) {
        List<ResponseSpeakerDetail> speakers = speakerService.getAllByLearningLibraryId(UUID.fromString(learningLibraryId));
        return responseSupport.success(ResponseData.builder().data(speakers).build());
    }

}
