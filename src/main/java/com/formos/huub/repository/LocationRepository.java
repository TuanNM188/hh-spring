package com.formos.huub.repository;

import com.formos.huub.domain.entity.Location;
import com.formos.huub.domain.enums.LocationTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findAllByGeoNameIdIn(List<String> geoNameIds);

    Optional<Location> findAllByGeoNameIdAndLocationType(String geoNameId, LocationTypeEnum locationType);

    Optional<Location> findOneByCodeAndLocationType(String code, LocationTypeEnum locationType);

    List<Location> findAllByCodeAndLocationType(String code, LocationTypeEnum locationType);

    List<Location> findAllByLocationTypeAndCodeIsNotNull(LocationTypeEnum locationType);

}
