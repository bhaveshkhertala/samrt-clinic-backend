package smart_clinic.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import smart_clinic.entity.Doctor;
import smart_clinic.repository.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // =========================================================
    // CREATE DOCTOR
    // =========================================================

    public Doctor createDoctor(Doctor doctor) {

        if (doctor.getName() == null ||
                doctor.getName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor name is required");
        }

        if (doctor.getUser() == null ||
                doctor.getUser().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor user account is required");
        }

        if (doctor.getClinic() == null ||
                doctor.getClinic().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Clinic is required");
        }

        return doctorRepository.save(doctor);
    }

    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    public List<Doctor> getAllDoctors() {

        return doctorRepository.findAll();
    }

    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    public Doctor getDoctorById(Long id) {

        return doctorRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Doctor not found with id: " + id));
    }

    // =========================================================
    // GET DOCTORS BY CLINIC
    // =========================================================

    public List<Doctor> getDoctorsByClinic(Long clinicId) {

        return doctorRepository.findByClinicId(clinicId);
    }

    // =========================================================
    // GET DOCTOR BY USER ID
    // =========================================================

    public Doctor getDoctorByUserId(Long userId) {

        return doctorRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Doctor profile not found for user id: "
                                        + userId));
    }

    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    public Doctor updateDoctor(
            Long id,
            Doctor updatedDoctor) {

        Doctor existingDoctor =
                doctorRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Doctor not found with id: " + id));

        existingDoctor.setName(
                updatedDoctor.getName());

        existingDoctor.setPhone(
                updatedDoctor.getPhone());

        existingDoctor.setSpecialization(
                updatedDoctor.getSpecialization());

        existingDoctor.setConsultationTime(
                updatedDoctor.getConsultationTime());

        existingDoctor.setClinic(
                updatedDoctor.getClinic());

        /*
         * Do not replace user during normal profile update.
         * The login account remains connected to this doctor.
         */

        return doctorRepository.save(existingDoctor);
    }

    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    public void deleteDoctor(Long id) {

        Doctor doctor =
                doctorRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Doctor not found with id: " + id));

        doctorRepository.delete(doctor);
    }
}