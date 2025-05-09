/**
 * ***************************************************
 * * Description :
 * * File        : PermissionRepository
 * * Author      : Hung Tran
 * * Date        : Oct 04, 2024
 * ***************************************************
 **/
package com.formos.huub.repository;

import com.formos.huub.domain.entity.Permission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

}
