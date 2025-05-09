package com.formos.huub.service.quartz;

import com.formos.huub.domain.entity.CommunityBoardPost;
import com.formos.huub.domain.entity.SchedulerJobInfo;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.quartz.SchedulerJobDetailsInfo;
import com.formos.huub.service.quartz.communityBoard.NotifyWhenPublicPost;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.quartz.JobDataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreateQuartzJobService {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTimeFormat.SLASH_YYYY_MM_DD_HH_MM_SS.getValue());

    SchedulerJobService schedulerJobService;

    public void deleteJobPublicPost(UUID postId) {
        String PUBLIC_POST = "PublicPost";
        String jobName = PUBLIC_POST + postId;
        schedulerJobService.deleteJob(jobName);
    }

    public void createJobPublicPost(List<String> mentionUserIds, CommunityBoardPost post) {
        String PUBLIC_POST = "PublicPost";
        String jobName = PUBLIC_POST + post.getId();
        schedulerJobService.deleteJob(jobName);
        JobDataMap map = new JobDataMap();
        map.put(NotifyWhenPublicPost.POST_ID, post.getId().toString());
        if (!CollectionUtils.isEmpty(mentionUserIds)) {
            map.put(NotifyWhenPublicPost.MENTION_USER_IDS, StringUtils.convertListToString(mentionUserIds));
        }
        var jobDetail = createCronJob(map, post.getScheduledTime(), PUBLIC_POST, jobName, NotifyWhenPublicPost.class);
        schedulerJobService.saveOrUpdate(jobDetail);
    }

    public SchedulerJobDetailsInfo createCronJob(JobDataMap map, Instant eventDate, String jobType, String jobName, Class<?> jobClass) {
        var timeEventMinusOneDay = eventDate.atZone(ZoneOffset.UTC).toLocalDateTime();
        SchedulerJobInfo job = schedulerJobService.newSchedulerJobInfo(
            jobName,
            jobType,
            jobClass.getName(),
            jobType + timeEventMinusOneDay.format(formatter),
            DateUtils.convertDateToCron(timeEventMinusOneDay)
        );
        var detailsInfo = new SchedulerJobDetailsInfo();
        detailsInfo.setSchedulerJobInfo(job);
        detailsInfo.setJobDataMap(map);
        return detailsInfo;
    }
}
