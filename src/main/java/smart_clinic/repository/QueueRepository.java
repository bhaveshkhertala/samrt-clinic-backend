package smart_clinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import smart_clinic.entity.Queue;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    Queue findTopByOrderByTokenNumberDesc();

    List<Queue> findByPatientId(Long patientId);

    List<Queue> findByPriority(String priority);

    List<Queue> findByStatusOrderByPriorityDescTokenNumberAsc(
            String status);

    List<Queue> findByDoctorClinicId(Long clinicId);

    long countByDoctorClinicIdAndStatus(Long clinicId, String status);

    List<Queue> findByDoctorIdAndStatus(Long doctorId, String status);
}