package smart_clinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import smart_clinic.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByPatientId(Long patientId);

    List<Notification> findByPatientIdAndStatus(Long patientId, String status);

    // Queue notifications include their token number in the message.
    @Transactional
    long deleteByPatient_IdAndMessageContaining(Long patientId, String messagePart);
}
