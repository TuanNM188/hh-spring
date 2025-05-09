package com.formos.huub.service.quartz;

import com.formos.huub.domain.entity.SchedulerJobInfo;
import com.formos.huub.quartz.JobScheduleCreator;
import com.formos.huub.quartz.SchedulerJobDetailsInfo;
import com.formos.huub.repository.SchedulerRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Transactional
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SchedulerJobService {

    Scheduler scheduler;
    SchedulerFactoryBean schedulerFactoryBean;
    SchedulerRepository schedulerRepository;
    ApplicationContext context;
    JobScheduleCreator scheduleCreator;

    public SchedulerJobService(
            Scheduler scheduler,
            SchedulerFactoryBean schedulerFactoryBean,
            SchedulerRepository schedulerRepository,
            ApplicationContext context,
            JobScheduleCreator scheduleCreator
    ) {
        this.scheduler = scheduler;
        this.schedulerFactoryBean = schedulerFactoryBean;
        this.schedulerRepository = schedulerRepository;
        this.context = context;
        this.scheduleCreator = scheduleCreator;
    }

    public SchedulerMetaData getMetaData() throws SchedulerException {
        return scheduler.getMetaData();
    }

    public List<SchedulerJobInfo> getAllJobList() {
        return schedulerRepository.findAll();
    }

    public boolean deleteJob(String jobInfoName) {
        try {
            Optional<SchedulerJobInfo> getJobInfo = schedulerRepository.findByJobName(jobInfoName);
            if (getJobInfo.isPresent()){
                schedulerRepository.delete(getJobInfo.get());
                return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(getJobInfo.get().getJobName(), getJobInfo.get().getJobGroup()));
            }
            return true;
        } catch (SchedulerException e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean deleteJobByNameAndGroup(String jobGroup, String jobInfoName) {
        try {
            Optional<SchedulerJobInfo> getJobInfo = schedulerRepository.findByJobGroupAndJobName(jobGroup, jobInfoName);
            if (getJobInfo.isPresent()){
                schedulerRepository.delete(getJobInfo.get());
                return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(getJobInfo.get().getJobName(), getJobInfo.get().getJobGroup()));
            }
            return true;
        } catch (SchedulerException e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean existJobByJobName(String jobInfoName) {
        return schedulerRepository.existsByJobName(jobInfoName);
    }

    public boolean pauseJob(SchedulerJobInfo jobInfo) {
        try {
            Optional<SchedulerJobInfo> getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
            getJobInfo.ifPresent(schedulerRepository::save);
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            return false;
        }
    }

    public boolean resumeJob(SchedulerJobInfo jobInfo) {
        try {
            Optional<SchedulerJobInfo> getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
            getJobInfo.ifPresent(schedulerRepository::save);
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            return false;
        }
    }

    public boolean startJobNow(SchedulerJobInfo jobInfo) {
        try {
            Optional<SchedulerJobInfo> getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
            getJobInfo.ifPresent(schedulerRepository::save);
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            return false;
        }
    }

    public void saveOrUpdateMultipleJob(List<SchedulerJobDetailsInfo> schedulerJobDetailsInfoList) {
        schedulerJobDetailsInfoList.forEach(jobDetailsInfo ->
                this.saveOrUpdate(jobDetailsInfo.getSchedulerJobInfo(), jobDetailsInfo.getJobDataMap())
        );
    }

    public void saveOrUpdate(SchedulerJobDetailsInfo jobDetailsInfo) {
        this.saveOrUpdate(jobDetailsInfo.getSchedulerJobInfo(), jobDetailsInfo.getJobDataMap());
    }

    public void saveOrUpdate(SchedulerJobInfo scheduleJob, JobDataMap jobDataMap) {
        if (scheduleJob.getCronExpression().length() > 0) {
            scheduleJob.setJobClass(scheduleJob.getJobClass());
            scheduleJob.setCronJob(true);
        } else {
            scheduleJob.setJobClass(scheduleJob.getJobClass());
            scheduleJob.setCronJob(false);
            scheduleJob.setRepeatTime((long) 1);
        }
        Optional<SchedulerJobInfo> getJobInfo = schedulerRepository.findByJobName(scheduleJob.getJobName());
        if (getJobInfo.isPresent()) {
            updateScheduleJob(scheduleJob, jobDataMap);
        } else {
            scheduleNewJob(scheduleJob, jobDataMap);
        }
        scheduleJob.setInterfaceName("interface_" + scheduleJob.getJobId());
    }

    private void scheduleNewJob(SchedulerJobInfo jobInfo, JobDataMap jobDataMap) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder
                    .newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
                    .build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                jobDetail =
                        scheduleCreator.createJob(
                                (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                                false,
                                context,
                                jobInfo.getJobName(),
                                jobInfo.getJobGroup()
                        );

                Trigger trigger;
                if (Boolean.TRUE.equals(jobInfo.getCronJob())) {
                    trigger =
                            scheduleCreator.createCronTrigger(
                                    jobInfo.getJobName(),
                                    new Date(),
                                    jobInfo.getCronExpression(),
                                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                            );
                } else {
                    trigger =
                            scheduleCreator.createSimpleTrigger(
                                    jobInfo.getJobName(),
                                    new Date(),
                                    jobInfo.getRepeatTime(),
                                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                            );
                }
                jobDetail.getJobDataMap().putAll(jobDataMap);
                scheduler.scheduleJob(jobDetail, trigger);
                jobInfo.setJobStatus("SCHEDULED");
                schedulerRepository.save(jobInfo);
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void updateScheduleJob(SchedulerJobInfo jobInfo, JobDataMap jobDataMap) {
        boolean isDelete = this.deleteJob(jobInfo.getJobName());
        try {
            if (isDelete) {
                Scheduler scheduler = schedulerFactoryBean.getScheduler();
                JobDetail jobDetail = scheduleCreator.createJob(
                        (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                        false,
                        context,
                        jobInfo.getJobName(),
                        jobInfo.getJobGroup()
                );
                Trigger newTrigger;
                if (Boolean.TRUE.equals(jobInfo.getCronJob())) {
                    newTrigger =
                            scheduleCreator.createCronTrigger(
                                    jobInfo.getJobName(),
                                    new Date(),
                                    jobInfo.getCronExpression(),
                                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                            );
                } else {
                    newTrigger =
                            scheduleCreator.createSimpleTrigger(
                                    jobInfo.getJobName(),
                                    new Date(),
                                    jobInfo.getRepeatTime(),
                                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                            );
                }
                jobDetail.getJobDataMap().putAll(jobDataMap);
                scheduler.scheduleJob(jobDetail, newTrigger);
                jobInfo.setJobGroup("EDITED & SCHEDULED");
                schedulerRepository.save(jobInfo);
            } else {
                log.error("scheduleUpdateJobRequest.jobDontExist");
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    public SchedulerJobInfo newSchedulerJobInfo(
            String jobName,
            String jobGroup,
            String jobClass,
            String description,
            String cronExpression
    ) {
        SchedulerJobInfo jobToLead = new SchedulerJobInfo();
        jobToLead.setJobName(jobName);
        jobToLead.setJobGroup(jobGroup);
        jobToLead.setJobClass(jobClass);
        jobToLead.setDescription(description);
        jobToLead.setCronExpression(cronExpression);
        return jobToLead;
    }
}
