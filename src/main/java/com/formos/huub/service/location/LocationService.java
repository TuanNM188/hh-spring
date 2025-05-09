package com.formos.huub.service.location;

import com.formos.huub.domain.entity.Location;
import com.formos.huub.domain.enums.LocationTypeEnum;
import com.formos.huub.domain.request.portals.RequestLocation;
import com.formos.huub.domain.response.location.ResponseLocationOption;
import com.formos.huub.mapper.location.LocationMapper;
import com.formos.huub.repository.LocationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {

    LocationRepository locationRepository;

    LocationMapper locationMapper;

    /**
     * Get location by type
     *
     * @param locationType LocationTypeEnum
     * @return List<ResponseLocation>
     */
    public List<ResponseLocationOption> getAllByType(LocationTypeEnum locationType) {
        return locationRepository.findAllByLocationTypeAndCodeIsNotNull(locationType).stream().map(locationMapper::toResponseLocationOption).toList();
    }

    /**
     * Create location
     *
     * @param requestLocations List<RequestLocation>
     */
    public void createLocation(List<RequestLocation> requestLocations) {
        List<Location> locationsToSave = new ArrayList<>();
        requestLocations.forEach(requestLocation -> {
            LocationTypeEnum locationType = LocationTypeEnum.valueOf(requestLocation.getType());
            boolean locationExists = locationRepository
                .findAllByGeoNameIdAndLocationType(requestLocation.getGeoNameId(), locationType)
                .isPresent();
            if (!locationExists) {
                Location location = locationMapper.toEntity(requestLocation, locationType);
                locationsToSave.add(location);
            }
        });

        if (!locationsToSave.isEmpty()) {
            locationRepository.saveAll(locationsToSave);
        }
    }
}
