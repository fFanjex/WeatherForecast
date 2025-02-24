package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ffanjex.weatherforecast.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
