package smart_clinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import smart_clinic.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndPasswordAndRole( String email, String password, String role );

}