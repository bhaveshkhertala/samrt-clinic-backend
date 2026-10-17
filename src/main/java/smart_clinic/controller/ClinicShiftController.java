package smart_clinic.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smart_clinic.entity.ClinicShift;
import smart_clinic.service.ClinicShiftService;

@RestController
@RequestMapping("/api/clinics/{clinicId}/shifts")
public class ClinicShiftController {

    private final ClinicShiftService clinicShiftService;

    public ClinicShiftController(ClinicShiftService clinicShiftService) {
        this.clinicShiftService = clinicShiftService;
    }

    @PostMapping
    public ClinicShift addShift(
            @PathVariable Long clinicId,
            @RequestBody ClinicShift shift) {
        return clinicShiftService.addShift(clinicId, shift);
    }

    @GetMapping
    public List<ClinicShift> getClinicShifts(
            @PathVariable Long clinicId) {
        return clinicShiftService.getClinicShifts(clinicId);
    }

    @GetMapping("/day/{dayOfWeek}")
    public List<ClinicShift> getClinicShiftsByDay(
            @PathVariable Long clinicId,
            @PathVariable String dayOfWeek) {
        return clinicShiftService.getClinicShiftsByDay(
                clinicId, dayOfWeek);
    }
    

    @DeleteMapping("/{shiftId}")
    public String deleteShift(
            @PathVariable Long clinicId,
            @PathVariable Long shiftId) {
        clinicShiftService.deleteShift(clinicId, shiftId);
        return "Clinic shift deleted successfully";
    }
}