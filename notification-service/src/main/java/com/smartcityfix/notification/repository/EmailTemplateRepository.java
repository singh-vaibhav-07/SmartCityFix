package com.smartcityfix.notification.repository;

import com.smartcityfix.notification.model.EmailTemplate;
import com.smartcityfix.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, NotificationType> {

}