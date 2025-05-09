package com.formos.huub.domain.response.specialty;

import java.util.UUID;

public interface IResponseSpecialtyUser {

    UUID getId();

    String getName();

    Integer getYearsOfExperience();

    String getSpecialtyAreaIds();

    String getSpecialtyAreaNames();
}
