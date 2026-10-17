package smart_clinic.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import smart_clinic.entity.User;
import smart_clinic.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================
    // CREATE USER
    // =========================================================

    @PostMapping
    public User createUser(@RequestBody User user) {

        return userService.createUser(user);
    }

    // =========================================================
    // GET ALL USERS
    // =========================================================

    @GetMapping
    public List<User> getAllUsers() {

        return userService.getAllUsers();
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> loginData) {

        String email = loginData.get("email");
        String password = loginData.get("password");
        String role = loginData.get("role");

        if (email == null || password == null || role == null) {
            return Map.of(
                "success", false,
                "message", "Email, password and role are required"
            );
        }

        User user = userService.login(email, password, role);

        if (user == null) {
            return Map.of(
                "success", false,
                "message", "Invalid email, password or account type"
            );
        }

        return Map.of(
            "success", true,
            "message", "Login successful",
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "phone", user.getPhone(),
            "role", user.getRole()
        );
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    @GetMapping("/{id}")
    public User getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }

    // =========================================================
    // UPDATE USER
    // =========================================================

    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        return userService.updateUser(id, user);
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @DeleteMapping("/{id}")
    public String deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}