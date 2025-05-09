package com.formos.huub.repository;

import com.formos.huub.domain.entity.ActivityLog;
import com.formos.huub.domain.response.activity.IResponseActivityLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    @Query(
        "SELECT a.id as id, a.login as login, a.activityType as activityType, a.deviceName as deviceName," +
            " a.deviceType as deviceType, a.deviceInfo as deviceInfo,a.operatingSystem as operatingSystem, a.createdDate as createdDate, a.userAgent as userAgent " +
            "FROM ActivityLog a " +
            "WHERE (:login is null OR a.login = :login) " +
            "AND (:searchKeyword IS NULL OR a.login LIKE :searchKeyword )" +
            "ORDER BY a.createdDate DESC"
    )
    Page<IResponseActivityLog> getActivityLogs(
        @Param("login") final String login,
        @Param("searchKeyword") final String searchKeyword,
        Pageable pageable
    );

    boolean existsByLoginAndActivityTypeAndAccessToken(
        final String login,
        final String activityType,
        final String accessToken
    );

    Optional<ActivityLog> findActivityLogByAccessTokenAndRefreshToken(
        final String accessToken,
        final String refreshToken
    );

    @Query("SELECT max(al.lastModifiedDate) AS lastModifiedDate FROM ActivityLog al" +
        " where al.login = :login group by al.login")
    Instant getLastActivityDate(String login);
}
