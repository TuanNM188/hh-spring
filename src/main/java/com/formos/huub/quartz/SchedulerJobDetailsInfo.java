package com.formos.huub.quartz;

import com.formos.huub.domain.entity.SchedulerJobInfo;
import lombok.Getter;
import lombok.Setter;
import org.quartz.JobDataMap;

@Getter
@Setter
public class SchedulerJobDetailsInfo {

    private SchedulerJobInfo schedulerJobInfo;
    private JobDataMap jobDataMap;
}
