package smart_clinic.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import smart_clinic.entity.Clinic;
import smart_clinic.entity.ClinicShift;
import smart_clinic.repository.ClinicRepository;
import smart_clinic.repository.ClinicShiftRepository;

@Service
public class ClinicShiftService {

    private final ClinicShiftRepository clinicShiftRepository;
    private final ClinicRepository clinicRepository;

    public ClinicShiftService(
            ClinicShiftRepository clinicShiftRepository,
            ClinicRepository clinicRepository) {
        this.clinicShiftRepository = clinicShiftRepository;
        this.clinicRepository = clinicRepository;
    }

    @Transactional
    public ClinicShift addShift(Long clinicId, ClinicShift shift) {

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Clinic not found"));

        validateShift(shift);

        shift.setDayOfWeek(
                shift.getDayOfWeek().trim().toUpperCase());

        shift.setClinic(clinic);

        return clinicShiftRepository.save(shift);
    }

    @Transactional(readOnly = true)
    public List<ClinicShift> getClinicShifts(Long clinicId) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Clinic not found");
        }

        return clinicShiftRepository.findByClinicId(clinicId);
    }

    @Transactional(readOnly = true)
    public List<ClinicShift> getClinicShiftsByDay(
            Long clinicId, String dayOfWeek) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Clinic not found");
        }

        validateDay(dayOfWeek);

        return clinicShiftRepository
                .findByClinicIdAndDayOfWeekIgnoreCase(
                        clinicId, dayOfWeek.trim());
    }

    private void validateShift(ClinicShift shift) {

        if (shift.getDayOfWeek() == null
                || shift.getDayOfWeek().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Day of week is required");
        }

        validateDay(shift.getDayOfWeek());

        LocalTime opening = shift.getOpeningTime();
        LocalTime closing = shift.getClosingTime();

        if (opening == null || closing == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Opening and closing times are required");
        }

        if (!opening.isBefore(closing)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Closing time must be after opening time");
        }
    }
    
    @Transactional
    public void deleteShift(Long clinicId, Long shiftId) {

        ClinicShift shift = clinicShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Clinic shift not found"));

        if (!shift.getClinic().getId().equals(clinicId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Shift does not belong to this clinic");
        }

        clinicShiftRepository.delete(shift);
    }
    

    private void validateDay(String day) {
        try {
            DayOfWeek.valueOf(day.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid day. Use MONDAY to SUNDAY");
        }
    }
}
