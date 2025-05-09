package com.formos.huub.domain.response.technicalassistance;

import java.time.Instant;
import java.util.UUID;

public interface IResponseInfoApplication {

    UUID getApplicationId();

    String getBusinessOwnerName();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getPhoneNumber();

    Instant getCreatedDate();

    Float getRemainingAwardHours();

    Instant getStartDate();

    Instant getEndDate();

    UUID getUserId();
}
