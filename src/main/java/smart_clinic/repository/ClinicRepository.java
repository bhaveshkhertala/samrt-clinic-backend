package smart_clinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import smart_clinic.entity.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    Optional<Clinic> findByOwnerEmailIgnoreCase(String email);
}
