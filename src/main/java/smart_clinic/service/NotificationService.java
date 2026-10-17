package smart_clinic.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import smart_clinic.entity.Notification;
import smart_clinic.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification) {
        if (notification.getMessage() == null || notification.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification message is required");
        }
        if (notification.getStatus() == null || notification.getStatus().isBlank()) {
            notification.setStatus("UNREAD");
        }
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found with id: " + id));
    }

    public List<Notification> getPatientNotifications(Long patientId) {
        return notificationRepository.findByPatientId(patientId);
    }

    public List<Notification> getUnreadNotifications(Long patientId) {
        return notificationRepository.findByPatientIdAndStatus(patientId, "UNREAD");
    }

    public Notification markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.setStatus("READ");
        return notificationRepository.save(notification);
    }

    public Notification markAsUnread(Long id) {
        Notification notification = getNotificationById(id);
        notification.setStatus("UNREAD");
        return notificationRepository.save(notification);
    }

    public List<Notification> markAllAsRead(Long patientId) {
        List<Notification> notifications = notificationRepository.findByPatientId(patientId);
        if (notifications.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No notifications found for patient id: " + patientId);
        }
        for (Notification notification : notifications) {
            notification.setStatus("READ");
        }
        return notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Long id) {
        Notification notification = getNotificationById(id);
        notificationRepository.delete(notification);
    }

    /** Deletes only notifications belonging to this patient and containing this globally assigned token number. */
    public void deleteQueueNotifications(Long patientId, Integer tokenNumber) {
        if (patientId == null || tokenNumber == null) return;
        notificationRepository.deleteByPatient_IdAndMessageContaining(
                patientId, "token number is " + tokenNumber);
        // The turn-approaching message uses "token number is N" too, so it is removed as well.
    }
}
