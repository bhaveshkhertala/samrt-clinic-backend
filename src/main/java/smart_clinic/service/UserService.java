package smart_clinic.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import smart_clinic.entity.Queue;
import smart_clinic.entity.User;
import smart_clinic.repository.QueueRepository;
import smart_clinic.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final QueueRepository queueRepository;

    public UserService(
            UserRepository userRepository,
            QueueRepository queueRepository) {

        this.userRepository = userRepository;
        this.queueRepository = queueRepository;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public User login(String email, String password, String role) {
        return userRepository
            .findByEmailAndPasswordAndRole(email, password, role)
            .orElse(null);
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    public User createUser(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required");
        }

        return userRepository.save(user);
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    public User getUserById(Long id) {

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id));
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    public User updateUser(
            Long id,
            User updatedUser) {

        User existingUser =
                userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id));

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    public void deleteUser(Long id) {

        User user =
                userRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with id: " + id));

        // Remove patient reference from queues
        List<Queue> queues =
                queueRepository.findByPatientId(id);

        for (Queue queue : queues) {
            queue.setPatient(null);
        }

        queueRepository.saveAll(queues);

        userRepository.delete(user);
    }
}