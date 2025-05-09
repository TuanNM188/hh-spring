package com.formos.huub.repository;

import com.formos.huub.domain.entity.InvoiceDetail;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

/**
 * ***************************************************
 * * Description :
 * * File        : InvoiceDetailRepository
 * * Author      : Hung Tran
 * * Date        : Mar 04, 2025
 * ***************************************************
 **/

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, UUID> {
    List<InvoiceDetail> findByInvoiceId(UUID invoiceId);

    @Modifying
    void deleteAllByInvoiceId(UUID questionId);
}
