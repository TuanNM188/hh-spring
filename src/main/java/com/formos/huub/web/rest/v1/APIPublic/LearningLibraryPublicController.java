package com.formos.huub.web.rest.v1.APIPublic;

import com.formos.huub.domain.request.learninglibrary.RequestSearchLearningLibrary;
import com.formos.huub.domain.response.categories.ResponseCategories;
import com.formos.huub.domain.response.speaker.ResponseSpeakerDetail;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.categories.CategoriesService;
import com.formos.huub.service.learninglibrary.LearningLibraryService;
import com.formos.huub.service.speaker.SpeakerService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/learning-libraries")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningLibraryPublicController {

    ResponseSupport responseSupport;
    CategoriesService categoriesService;
    SpeakerService speakerService;
    LearningLibraryService learningLibraryService;

    @GetMapping("/categories")
    public ResponseEntity<ResponseData> getAllCategory(@RequestParam(required = false) boolean hasServices) {
        List<ResponseCategories> categories = categoriesService.getAll();
        if (hasServices) {
            categories = categoriesService.getAllCategoriesAndServices();
        }
        return responseSupport.success(ResponseData.builder().data(categories).build());
    }

    @GetMapping("/speakers")
    public ResponseEntity<ResponseData> getAllSpeaker() {
        List<ResponseSpeakerDetail> speakers = speakerService.getAll();
        return responseSupport.success(ResponseData.builder().data(speakers).build());
    }

    @PostMapping("/card-view")
    public ResponseEntity<ResponseData> searchCardView(@RequestBody RequestSearchLearningLibrary request) {
        return responseSupport.success(
            ResponseData.builder().data(learningLibraryService.searchLearningLibrariesCardView(request)).build()
        );
    }
}
