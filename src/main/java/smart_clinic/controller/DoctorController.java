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

import smart_clinic.entity.Doctor;
import smart_clinic.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(
            DoctorService doctorService) {

        this.doctorService = doctorService;
    }

    // =========================================================
    // CREATE DOCTOR
    // =========================================================

    @PostMapping
    public Doctor createDoctor(
            @RequestBody Doctor doctor) {

        return doctorService.createDoctor(doctor);
    }

    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    @GetMapping
    public List<Doctor> getAllDoctors() {

        return doctorService.getAllDoctors();
    }

    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    @GetMapping("/{id}")
    public Doctor getDoctorById(
            @PathVariable Long id) {

        return doctorService.getDoctorById(id);
    }

    // =========================================================
    // GET DOCTORS BY CLINIC
    // =========================================================

    @GetMapping("/clinic/{clinicId}")
    public List<Doctor> getDoctorsByClinic(
            @PathVariable Long clinicId) {

        return doctorService.getDoctorsByClinic(clinicId);
    }

    // =========================================================
    // GET DOCTOR BY USER ID
    // =========================================================

    @GetMapping("/user/{userId}")
    public Doctor getDoctorByUserId(
            @PathVariable Long userId) {

        return doctorService.getDoctorByUserId(userId);
    }

    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    @PutMapping("/{id}")
    public Doctor updateDoctor(
            @PathVariable Long id,
            @RequestBody Doctor doctor) {

        return doctorService.updateDoctor(
                id,
                doctor);
    }

    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    @DeleteMapping("/{id}")
    public String deleteDoctor(
            @PathVariable Long id) {

        doctorService.deleteDoctor(id);

        return "Doctor deleted successfully";
    }
}