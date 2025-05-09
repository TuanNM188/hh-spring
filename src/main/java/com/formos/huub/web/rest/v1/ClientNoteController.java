package com.formos.huub.web.rest.v1;


import com.formos.huub.domain.request.clientnote.RequestSearchClientNotes;
import com.formos.huub.domain.request.clientnote.RequestCreateClientNote;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.service.clientnote.ClientNoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client-note")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientNoteController {

    ResponseSupport responseSupport;
    ClientNoteService clientNoteService;

    @GetMapping("/{clientNodeId}")
    public ResponseEntity<ResponseData> getClientNoteById(
        @PathVariable String clientNodeId
    ) {
        return responseSupport.success(
            ResponseData.builder().data(clientNoteService.getClientNoteById(clientNodeId)).build()
        );
    }

    @PostMapping()
    public ResponseEntity<ResponseData> createClientNote(
        @RequestBody @Valid RequestCreateClientNote request
    ) {
        return responseSupport.success(
            ResponseData.builder().data(clientNoteService.createClientNote(request)).build()
        );
    }

    @PostMapping("/{userId}/search")
    public ResponseEntity<ResponseData> searchClientNotes(
        @PathVariable String userId,
        @RequestBody RequestSearchClientNotes request, HttpServletRequest httpServletRequest
    ) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(clientNoteService.searchClientNotesByUser(userId, request)).build());
    }
}
