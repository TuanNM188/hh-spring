package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.LocationTypeEnum;
import com.formos.huub.domain.request.location.RequestCreateLocation;
import com.formos.huub.domain.response.location.ResponseLocationOption;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.location.LocationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {

    LocationService locationService;

    ResponseSupport responseSupport;

    @GetMapping
    public ResponseEntity<ResponseData> getAllCategory(@RequestParam(required = false) String type) {
        List<ResponseLocationOption> locations = locationService.getAllByType(LocationTypeEnum.valueOf(type));
        return responseSupport.success(ResponseData.builder().data(locations).build());
    }

    @PostMapping
    public ResponseEntity<ResponseData> createCategories(@RequestBody @Valid RequestCreateLocation request) {
        locationService.createLocation(request.getLocations());
        return responseSupport.success(ResponseData.builder().build());
    }
}
