package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.PermissionTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * ***************************************************
 * * Description :
 * * File        : Permission
 * * Author      : Hung Tran
 * * Date        : Otc 04, 2024
 * ***************************************************
 **/

@Entity
@Table(name = "permission")
@Getter
@Setter
public class Permission implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PermissionTypeEnum type;

}
