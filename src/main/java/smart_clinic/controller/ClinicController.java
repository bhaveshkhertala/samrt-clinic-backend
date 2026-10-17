
package smart_clinic.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smart_clinic.entity.Clinic;
import smart_clinic.service.ClinicService;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @PostMapping("/register-owner")
    public Clinic registerClinicOwner(
            @RequestBody Map<String, String> data) {
        return clinicService.registerClinicOwner(data);
    }

    @PostMapping
    public Clinic createClinic(@RequestBody Clinic clinic) {
        return clinicService.createClinic(clinic);
    }

    @GetMapping
    public List<Clinic> getAllClinics() {
        return clinicService.getAllClinics();
    }

    @GetMapping("/{id}")
    public Clinic getClinicById(@PathVariable Long id) {
        return clinicService.getClinicById(id);
    }

    @GetMapping("/my-dashboard")
    public Map<String, Object> getMyClinicDashboard(
            @RequestParam String email) {
        return clinicService.getClinicDashboardByEmail(email);
    }

    @PutMapping("/{id}")
    public Clinic updateClinic(
            @PathVariable Long id,
            @RequestBody Clinic clinic) {
        return clinicService.updateClinic(id, clinic);
    }

    @DeleteMapping("/{id}")
    public String deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return "Clinic deleted successfully";
    }
}
