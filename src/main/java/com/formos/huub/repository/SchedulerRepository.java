package com.formos.huub.repository;

import com.formos.huub.domain.entity.SchedulerJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerJobInfo, Long> {

    Optional<SchedulerJobInfo> findByJobName(String jobName);
    Optional<SchedulerJobInfo> findByJobGroupAndJobName(String jobGroup, String jobName);
    Boolean existsByJobName(String jobName);

}

