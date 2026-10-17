package smart_clinic.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import smart_clinic.entity.Queue;
import smart_clinic.service.QueueService;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public Queue createQueue(@RequestBody Queue queue) {
        return queueService.createQueue(queue);
    }

    @GetMapping
    public List<Queue> getAllQueues() {
        return queueService.getAllQueues();
    }

    @GetMapping("/{id}")
    public Queue getQueueById(@PathVariable Long id) {
        return queueService.getQueueById(id);
    }

    @GetMapping("/patient/{patientId}")
    public List<Queue> getPatientQueue(@PathVariable Long patientId) {
        return queueService.getPatientQueue(patientId);
    }

    @GetMapping("/clinic/{clinicId}")
    public List<Queue> getClinicQueues(@PathVariable Long clinicId) {
        return queueService.getClinicQueues(clinicId);
    }

    @GetMapping("/clinic/{clinicId}/current-patients")
    public long getCurrentPatientsCount(@PathVariable Long clinicId) {
        return queueService.getCurrentPatientsCount(clinicId);
    }

    @GetMapping("/priority")
    public List<Queue> getPriorityQueues() {
        return queueService.getPriorityQueues();
    }

    @GetMapping("/next")
    public Queue getNextPatient() {
        return queueService.getNextPatient();
    }

    @GetMapping("/{id}/patients-ahead")
    public long getPatientsAhead(@PathVariable Long id) {
        return queueService.getPatientsAhead(id);
    }

    @GetMapping("/{id}/wait-time")
    public int getEstimatedWaitTime(@PathVariable Long id) {
        return queueService.calculateEstimatedWaitTime(id);
    }

    @PutMapping("/{id}")
    public Queue updateQueue(
            @PathVariable Long id,
            @RequestBody Queue queue) {
        return queueService.updateQueue(id, queue);
    }

    // Legacy endpoint: selects the next available queue.
    @PutMapping("/call-next")
    public Queue callNextPatient() {
        return queueService.callNextPatient();
    }

    // Preferred endpoint: selects next patient for this doctor only.
    @PutMapping("/call-next/{doctorId}")
    public Queue callNextPatientForDoctor(
            @PathVariable Long doctorId) {
        return queueService.callNextPatient(doctorId);
    }

    @PutMapping("/complete/{id}")
    public Queue completePatient(@PathVariable Long id) {
        return queueService.completePatient(id);
    }

    @DeleteMapping("/{id}")
    public String deleteQueue(@PathVariable Long id) {
        queueService.deleteQueue(id);
        return "Queue deleted successfully";
    }
}