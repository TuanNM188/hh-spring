package com.formos.huub.domain.response.technicalassistance;

import java.util.UUID;

public interface IResponseTermVendor {
    UUID getId();

    String getName();

    UUID getVendorId();

    Float getCalculatedHours();
}
