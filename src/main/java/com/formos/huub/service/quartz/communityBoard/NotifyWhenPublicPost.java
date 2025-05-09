package com.formos.huub.service.quartz.communityBoard;

import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.communityboard.CommunityBoardPostService;
import com.formos.huub.service.quartz.CreateQuartzJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@DisallowConcurrentExecution
@Service
public class NotifyWhenPublicPost extends QuartzJobBean {

    @Autowired
    private CommunityBoardPostService postService;

    @Autowired
    private CreateQuartzJobService createQuartzJobService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    public static final String POST_ID;
    public static final String MENTION_USER_IDS;

    static {
        POST_ID = "post_id";
        MENTION_USER_IDS = "mention_user_ids";
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        UUID postId = UUIDUtils.convertToUUID(context.getJobDetail().getJobDataMap().getString(POST_ID));
        log.info("Start Job NotifyWhenPublicPost for Post ID:", postId);
        String mentionUserIdsAsString = context.getJobDetail().getJobDataMap().getString(MENTION_USER_IDS);
        List<UUID> mentionUserIds = new ArrayList<>();
        if (StringUtils.isNotBlank(mentionUserIdsAsString)) {
            mentionUserIds = UUIDUtils.toUUIDs(Arrays.asList(mentionUserIdsAsString.split(",")));
        }

        postService.publicPostWhenScheduleTime(mentionUserIds, postId);
        createQuartzJobService.deleteJobPublicPost(postId);
    }
}
