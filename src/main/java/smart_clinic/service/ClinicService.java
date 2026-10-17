
package smart_clinic.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import smart_clinic.entity.Clinic;
import smart_clinic.entity.Queue;
import smart_clinic.entity.User;
import smart_clinic.repository.ClinicRepository;
import smart_clinic.repository.QueueRepository;
import smart_clinic.repository.UserRepository;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;

    public ClinicService(
            ClinicRepository clinicRepository,
            QueueRepository queueRepository,
            UserRepository userRepository) {
        this.clinicRepository = clinicRepository;
        this.queueRepository = queueRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Clinic registerClinicOwner(Map<String, String> data) {

        String ownerName = data.get("ownerName");
        String email = data.get("email");
        String phone = data.get("phone");
        String password = data.get("password");
        String clinicName = data.get("clinicName");
        String address = data.get("address");
        String city = data.get("city");
        String clinicPhone = data.get("clinicPhone");

        if (ownerName == null || ownerName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || clinicName == null || clinicName.isBlank()
                || address == null || address.isBlank()
                || city == null || city.isBlank()
                || clinicPhone == null || clinicPhone.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please fill all required fields");
        }

        String normalizedEmail = email.trim();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered");
        }

        User owner = new User();
        owner.setName(ownerName.trim());
        owner.setEmail(normalizedEmail);
        owner.setPhone(phone);
        owner.setPassword(password);
        owner.setRole("CLINIC");

        owner = userRepository.save(owner);

        Clinic clinic = new Clinic();
        clinic.setName(clinicName.trim());
        clinic.setAddress(address.trim());
        clinic.setCity(city.trim());
        clinic.setPhone(clinicPhone.trim());
        clinic.setTotalDoctors(0);
        clinic.setCurrentPatients(0);
        clinic.setOwner(owner);

        return clinicRepository.save(clinic);
    }

    public Clinic createClinic(Clinic clinic) {
        if (clinic.getName() == null || clinic.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Clinic name is required");
        }

        return clinicRepository.save(clinic);
    }

    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    public Clinic getClinicById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Clinic not found with id: " + id));
    }

    public Clinic updateClinic(Long id, Clinic updatedClinic) {
        Clinic existingClinic = getClinicById(id);

        existingClinic.setName(updatedClinic.getName());
        existingClinic.setAddress(updatedClinic.getAddress());
        existingClinic.setCity(updatedClinic.getCity());
        existingClinic.setPhone(updatedClinic.getPhone());
        existingClinic.setTotalDoctors(updatedClinic.getTotalDoctors());
        existingClinic.setCurrentPatients(updatedClinic.getCurrentPatients());

        return clinicRepository.save(existingClinic);
    }

    public void deleteClinic(Long id) {
        Clinic clinic = getClinicById(id);
        clinicRepository.delete(clinic);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClinicDashboardByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Clinic owner email is required");
        }

        Clinic clinic = clinicRepository
                .findByOwnerEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No clinic found for this owner"));

        Long clinicId = clinic.getId();

        List<Queue> queues =
                queueRepository.findByDoctorClinicId(clinicId);

        long waitingPatients =
                queueRepository.countByDoctorClinicIdAndStatus(
                        clinicId, "WAITING");

        long inProgressPatients =
                queueRepository.countByDoctorClinicIdAndStatus(
                        clinicId, "IN_PROGRESS");

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("clinic", clinic);
        dashboard.put("totalDoctors", clinic.getTotalDoctors());
        dashboard.put("waitingPatients", waitingPatients);
        dashboard.put("inProgressPatients", inProgressPatients);
        dashboard.put("totalQueueTokens", queues.size());
        dashboard.put("queues", queues);

        return dashboard;
    }
}
