package com.formos.huub.repository;

import com.formos.huub.domain.entity.AppointmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetail, UUID>   {
    Optional<AppointmentDetail> findByAppointmentId(UUID appointmentId);
}
