package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ffanjex.weatherforecast.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
