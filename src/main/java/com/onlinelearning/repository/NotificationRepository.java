package com.onlinelearning.repository;

import com.onlinelearning.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUserAccountIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserUserAccountIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserUserAccountIdAndIsReadFalse(Long userId);

    List<Notification> findByUserUserAccountIdAndIsReadFalse(Long userId);
}