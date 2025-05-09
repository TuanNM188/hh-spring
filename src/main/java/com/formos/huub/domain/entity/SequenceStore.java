package com.formos.huub.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.*;

/**
 * ***************************************************
 * * Description :
 * * File        : SequenceGenerator
 * * Author      : Hung Tran
 * * Date        : Mar 05, 2025
 * ***************************************************
 **/

@Entity
@Table(name = "sequence_store")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceStore {

    @Id
    @Column(name = "sequence_name", nullable = false)
    private String sequenceName;

    @Column(name = "current_value", nullable = false)
    private Long currentValue;

    @Column(name = "increment_size", nullable = false)
    private Integer incrementSize;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
}
