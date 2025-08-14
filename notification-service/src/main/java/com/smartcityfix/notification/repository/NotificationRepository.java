package com.smartcityfix.notification.repository;

import com.smartcityfix.notification.model.Notification;
import com.smartcityfix.notification.model.NotificationChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndRead(UUID userId, boolean read, Pageable pageable);

    List<Notification> findByUserIdAndSent(UUID userId, boolean sent);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.channel = :channel AND n.sent = false")
    List<Notification> findPendingNotifications(@Param("userId") UUID userId, @Param("channel") NotificationChannel channel);

    long countByUserIdAndRead(UUID userId, boolean read);
}