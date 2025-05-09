package com.formos.huub.repository;

import com.formos.huub.domain.request.vendor.RequestSearchVendor;
import com.formos.huub.domain.response.vendor.ResponseSearchVendors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProgramTermVendorRepositoryCustom {
    Page<ResponseSearchVendors> searchByTermAndCondition(RequestSearchVendor condition, Pageable pageable);
}
