
package smart_clinic.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import smart_clinic.entity.ClinicShift;
import smart_clinic.entity.Doctor;
import smart_clinic.entity.Notification;
import smart_clinic.entity.Queue;
import smart_clinic.repository.ClinicShiftRepository;
import smart_clinic.repository.DoctorRepository;
import smart_clinic.repository.QueueRepository;

@Service
public class QueueService {

    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    private final QueueRepository queueRepository;
    private final NotificationService notificationService;
    private final ClinicShiftRepository clinicShiftRepository;
    private final DoctorRepository doctorRepository;

    public QueueService(
            QueueRepository queueRepository,
            NotificationService notificationService,
            ClinicShiftRepository clinicShiftRepository,
            DoctorRepository doctorRepository) {

        this.queueRepository = queueRepository;
        this.notificationService = notificationService;
        this.clinicShiftRepository = clinicShiftRepository;
        this.doctorRepository = doctorRepository;
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private Long getDoctorId(Queue queue) {
        return queue.getDoctor() == null ? null : queue.getDoctor().getId();
    }

    private Long getPatientId(Queue queue) {
        return queue.getPatient() == null ? null : queue.getPatient().getId();
    }

    /*
     * Fetch the persisted doctor so its clinic relationship is available.
     * Booking availability is controlled by clinic hours, not doctor hours.
     */
    private Doctor getDoctorOrThrow(Long doctorId) {
        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Doctor ID is required");
        }

        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> notFound("Doctor not found with id: " + doctorId));
    }

    /*
     * Checks today's clinic shifts using Indian Standard Time.
     * Opening time is inclusive; closing time is exclusive.
     */
    private void validateClinicIsOpen(Doctor doctor) {

        if (doctor.getClinic() == null || doctor.getClinic().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected doctor is not assigned to a clinic");
        }

        Long clinicId = doctor.getClinic().getId();
        LocalDate today = LocalDate.now(INDIA_ZONE);
        LocalTime now = LocalTime.now(INDIA_ZONE);
        String dayOfWeek = today.getDayOfWeek().name();

        List<ClinicShift> todaysShifts =
                clinicShiftRepository.findByClinicIdAndDayOfWeekIgnoreCase(
                        clinicId, dayOfWeek);

        if (todaysShifts == null || todaysShifts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Token booking is unavailable because this clinic has no working hours configured for today.");
        }

        boolean clinicIsOpen = todaysShifts.stream().anyMatch(shift -> {
            if (shift == null
                    || shift.getOpeningTime() == null
                    || shift.getClosingTime() == null) {
                return false;
            }

            LocalTime opening = shift.getOpeningTime();
            LocalTime closing = shift.getClosingTime();

            // Overnight shifts are not supported by this validation.
            if (!opening.isBefore(closing)) {
                return false;
            }

            return !now.isBefore(opening) && now.isBefore(closing);
        });

        if (!clinicIsOpen) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This clinic is currently closed. Please book your token during the clinic's working hours.");
        }
    }

    private Comparator<Queue> queueOrder() {
        return Comparator.comparingInt((Queue q) ->
                        "PRIORITY".equalsIgnoreCase(q.getPriority()) ? 0 : 1)
                .thenComparing(
                        Queue::getTokenNumber,
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(
                        Queue::getId,
                        Comparator.nullsLast(Long::compareTo));
    }

    private List<Queue> getWaitingForDoctor(Long doctorId) {
        List<Queue> waiting =
                queueRepository.findByDoctorIdAndStatus(doctorId, "WAITING");
        waiting.sort(queueOrder());
        return waiting;
    }

    private int getConsultationTime(Queue queue) {
        if (queue.getDoctor() == null
                || queue.getDoctor().getConsultationTime() == null) {
            return 15;
        }

        return Math.max(1, queue.getDoctor().getConsultationTime());
    }

    private long countInProgress(Long doctorId) {
        return queueRepository
                .findByDoctorIdAndStatus(doctorId, "IN_PROGRESS")
                .size();
    }

    private int calculateWaitForQueue(
            Queue queue, List<Queue> orderedWaiting) {

        if (!"WAITING".equalsIgnoreCase(queue.getStatus())) {
            return 0;
        }

        Long doctorId = getDoctorId(queue);
        if (doctorId == null) {
            return 0;
        }

        int ahead = 0;
        boolean found = false;

        for (Queue waiting : orderedWaiting) {
            if (Objects.equals(waiting.getId(), queue.getId())) {
                found = true;
                break;
            }
            ahead++;
        }

        if (!found) {
            return 0;
        }

        return (int) ((ahead + countInProgress(doctorId))
                * getConsultationTime(queue));
    }

    @Transactional
    public Queue createQueue(Queue queue) {

        if (queue == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Queue request is required");
        }

        if (queue.getPatient() == null || queue.getPatient().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Patient is required");
        }

        if (queue.getDoctor() == null || queue.getDoctor().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Doctor is required");
        }

        // Load the actual doctor and its clinic from the database.
        Doctor doctor = getDoctorOrThrow(queue.getDoctor().getId());
        queue.setDoctor(doctor);

        // Validate before saving any token.
        validateClinicIsOpen(doctor);

        Long patientId = queue.getPatient().getId();

        List<Queue> existingQueues =
                queueRepository.findByPatientId(patientId);

        boolean hasActiveToken = existingQueues.stream().anyMatch(existing -> {
            String status = existing.getStatus() == null
                    ? ""
                    : existing.getStatus().toUpperCase();

            return "WAITING".equals(status)
                    || "IN_PROGRESS".equals(status);
        });

        if (hasActiveToken) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have an active token. You can book another token only after your current consultation is completed.");
        }

        Queue lastQueue = queueRepository.findTopByOrderByTokenNumberDesc();

        int nextToken = lastQueue == null || lastQueue.getTokenNumber() == null
                ? 1
                : lastQueue.getTokenNumber() + 1;

        queue.setTokenNumber(nextToken);
        queue.setStatus("WAITING");

        if (queue.getPriority() == null || queue.getPriority().isBlank()) {
            queue.setPriority("NORMAL");
        }

        queue.setEstimatedWaitTime(0);

        Queue saved = queueRepository.save(queue);

        refreshWaitingTimes(getDoctorId(saved));
        saved = queueRepository.findById(saved.getId()).orElse(saved);

        String clinicName = "your selected clinic";

        if (saved.getDoctor() != null
                && saved.getDoctor().getClinic() != null
                && saved.getDoctor().getClinic().getName() != null
                && !saved.getDoctor().getClinic().getName().isBlank()) {
            clinicName = saved.getDoctor().getClinic().getName();
        }

        createNotification(
                saved,
                "QUEUE",
                "Your token number is " + saved.getTokenNumber()
                        + ". You have joined the " + clinicName + " clinic queue."
                        + " Your estimated wait is "
                        + saved.getEstimatedWaitTime() + " minutes.");

        notifyApproachingPatients(getDoctorId(saved));

        return saved;
    }

    public List<Queue> getAllQueues() {
        return queueRepository.findAll();
    }

    public Queue getQueueById(Long id) {
        return queueRepository.findById(id)
                .orElseThrow(() -> notFound("Queue not found with id: " + id));
    }

    public List<Queue> getPatientQueue(Long patientId) {
        return queueRepository.findByPatientId(patientId);
    }

    public List<Queue> getClinicQueues(Long clinicId) {
        return queueRepository.findByDoctorClinicId(clinicId);
    }

    public long getCurrentPatientsCount(Long clinicId) {
        return queueRepository.countByDoctorClinicIdAndStatus(
                clinicId, "WAITING");
    }

    public List<Queue> getPriorityQueues() {
        List<Queue> result = queueRepository.findByPriority("PRIORITY");
        result.removeIf(q -> !"WAITING".equalsIgnoreCase(q.getStatus()));
        result.sort(queueOrder());
        return result;
    }

    public Queue getNextPatient() {
        List<Queue> waiting =
                queueRepository.findByStatusOrderByPriorityDescTokenNumberAsc(
                        "WAITING");

        waiting.sort(queueOrder());
        return waiting.isEmpty() ? null : waiting.get(0);
    }

    @Transactional
    public Queue callNextPatient() {
        Queue next = getNextPatient();

        if (next == null) {
            throw notFound("No waiting patient found");
        }

        Long doctorId = getDoctorId(next);

        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The next patient has no assigned doctor");
        }

        return callNextPatient(doctorId);
    }

    @Transactional
    public Queue callNextPatient(Long doctorId) {
        if (doctorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Doctor ID is required");
        }

        if (countInProgress(doctorId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This doctor already has a patient in progress");
        }

        List<Queue> waiting = getWaitingForDoctor(doctorId);

        if (waiting.isEmpty()) {
            throw notFound("No waiting patient found for this doctor");
        }

        Queue next = waiting.get(0);
        next.setStatus("IN_PROGRESS");
        next.setEstimatedWaitTime(0);

        Queue saved = queueRepository.save(next);

        createNotification(
                saved, "TURN",
                "Your turn has arrived. Please proceed to the doctor.");

        refreshWaitingTimes(doctorId);
        notifyApproachingPatients(doctorId);

        return saved;
    }

    @Transactional
    public Queue completePatient(Long id) {
        Queue queue = getQueueById(id);

        Long doctorId = getDoctorId(queue);
        Long patientId = getPatientId(queue);
        Integer tokenNumber = queue.getTokenNumber();

        queue.setStatus("COMPLETED");
        queue.setEstimatedWaitTime(0);

        Queue saved = queueRepository.save(queue);

        notificationService.deleteQueueNotifications(patientId, tokenNumber);

        if (doctorId != null) {
            refreshWaitingTimes(doctorId);
            notifyApproachingPatients(doctorId);
        }

        return saved;
    }

    public long getPatientsAhead(Long queueId) {
        Queue current = getQueueById(queueId);

        if (!"WAITING".equalsIgnoreCase(current.getStatus())
                || getDoctorId(current) == null) {
            return 0;
        }

        List<Queue> waiting = getWaitingForDoctor(getDoctorId(current));

        long count = 0;

        for (Queue q : waiting) {
            if (Objects.equals(q.getId(), current.getId())) {
                return count;
            }
            count++;
        }

        return 0;
    }

    @Transactional
    public int calculateEstimatedWaitTime(Long queueId) {
        Queue current = getQueueById(queueId);

        if (!"WAITING".equalsIgnoreCase(current.getStatus())
                || getDoctorId(current) == null) {
            return 0;
        }

        int estimatedWait = calculateWaitForQueue(
                current, getWaitingForDoctor(getDoctorId(current)));

        current.setEstimatedWaitTime(estimatedWait);
        queueRepository.save(current);

        return estimatedWait;
    }

    @Transactional
    public Queue updateQueue(Long id, Queue updated) {
        Queue existing = getQueueById(id);
        Long oldDoctorId = getDoctorId(existing);

        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        if (updated.getPriority() != null) {
            existing.setPriority(updated.getPriority());
        }

        if (updated.getDoctor() != null && updated.getDoctor().getId() != null) {
            existing.setDoctor(getDoctorOrThrow(updated.getDoctor().getId()));
        }

        Queue saved = queueRepository.save(existing);
        Long newDoctorId = getDoctorId(saved);

        if ("COMPLETED".equalsIgnoreCase(saved.getStatus())) {
            notificationService.deleteQueueNotifications(
                    getPatientId(saved), saved.getTokenNumber());
        }

        if (oldDoctorId != null) {
            refreshWaitingTimes(oldDoctorId);
            notifyApproachingPatients(oldDoctorId);
        }

        if (newDoctorId != null && !Objects.equals(oldDoctorId, newDoctorId)) {
            refreshWaitingTimes(newDoctorId);
            notifyApproachingPatients(newDoctorId);
        }

        return queueRepository.findById(id).orElse(saved);
    }

    @Transactional
    public void deleteQueue(Long id) {
        Queue queue = getQueueById(id);
        Long doctorId = getDoctorId(queue);

        queueRepository.delete(queue);

        if (doctorId != null) {
            refreshWaitingTimes(doctorId);
            notifyApproachingPatients(doctorId);
        }
    }

    private void refreshWaitingTimes(Long doctorId) {
        if (doctorId == null) {
            return;
        }

        List<Queue> waiting = getWaitingForDoctor(doctorId);
        long inProgress = countInProgress(doctorId);

        for (int i = 0; i < waiting.size(); i++) {
            Queue queue = waiting.get(i);

            queue.setEstimatedWaitTime(
                    (int) ((i + inProgress) * getConsultationTime(queue)));
        }

        queueRepository.saveAll(waiting);
    }

    private void createNotification(
            Queue queue, String type, String message) {

        if (queue.getPatient() == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setType(type);
        notification.setStatus("UNREAD");
        notification.setPatient(queue.getPatient());

        notificationService.createNotification(notification);
    }

    private void notifyApproachingPatients(Long doctorId) {
        if (doctorId == null) {
            return;
        }

        List<Queue> waiting = getWaitingForDoctor(doctorId);

        if (waiting.size() < 2) {
            return;
        }

        Queue queue = waiting.get(1);
        Long patientId = getPatientId(queue);

        if (patientId == null) {
            return;
        }

        String message = "Your turn is approaching. Please be ready. "
                + "Your token number is " + queue.getTokenNumber() + ".";

        boolean alreadySent = notificationService
                .getPatientNotifications(patientId)
                .stream()
                .anyMatch(n -> "QUEUE".equals(n.getType())
                        && message.equals(n.getMessage()));

        if (!alreadySent) {
            createNotification(queue, "QUEUE", message);
        }
    }
}
