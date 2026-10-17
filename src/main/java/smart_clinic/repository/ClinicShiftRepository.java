package smart_clinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import smart_clinic.entity.ClinicShift;

public interface ClinicShiftRepository
        extends JpaRepository<ClinicShift, Long> {

    List<ClinicShift> findByClinicId(Long clinicId);

    List<ClinicShift> findByClinicIdAndDayOfWeekIgnoreCase(
            Long clinicId, String dayOfWeek);
}
