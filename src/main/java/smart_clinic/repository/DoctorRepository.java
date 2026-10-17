package smart_clinic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import smart_clinic.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByClinicId(Long clinicId);

    Optional<Doctor> findByUserId(Long userId);
}