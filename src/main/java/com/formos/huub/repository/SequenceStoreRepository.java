package com.formos.huub.repository;

import com.formos.huub.domain.entity.SequenceStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ***************************************************
 * * Description :
 * * File        : SequenceStoreRepository
 * * Author      : Hung Tran
 * * Date        : Mar 05, 2025
 * ***************************************************
 **/

@Repository
public interface SequenceStoreRepository extends JpaRepository<SequenceStore, String> {
    @Query(value = "SELECT get_next_sequence_value(:sequenceName)", nativeQuery = true)
    Long getNextSequenceValue(@Param("sequenceName") String sequenceName);
}
