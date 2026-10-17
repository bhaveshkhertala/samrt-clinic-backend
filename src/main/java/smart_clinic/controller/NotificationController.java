
package smart_clinic.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smart_clinic.entity.Notification;
import smart_clinic.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Create Notification
    @PostMapping
    public Notification createNotification(
            @RequestBody Notification notification) {
        return notificationService.createNotification(notification);
    }

    // Get All Notifications
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    // Get Notification By ID
    @GetMapping("/{id}")
    public Notification getNotificationById(
            @PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    // Get Patient Notifications
    @GetMapping("/patient/{patientId}")
    public List<Notification> getPatientNotifications(
            @PathVariable Long patientId) {
        return notificationService.getPatientNotifications(patientId);
    }

    // Get Unread Notifications
    @GetMapping("/patient/{patientId}/unread")
    public List<Notification> getUnreadNotifications(
            @PathVariable Long patientId) {
        return notificationService.getUnreadNotifications(patientId);
    }

    // Mark One Notification As Read
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    // Mark One Notification As Unread
    @PutMapping("/{id}/unread")
    public Notification markAsUnread(@PathVariable Long id) {
        return notificationService.markAsUnread(id);
    }

    // Mark All Notifications Of A Patient As Read
    @PutMapping("/patient/{patientId}/read-all")
    public List<Notification> markAllAsRead(
            @PathVariable Long patientId) {
        return notificationService.markAllAsRead(patientId);
    }

    // Delete Notification
    @DeleteMapping("/{id}")
    public String deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return "Notification deleted successfully";
    }
}