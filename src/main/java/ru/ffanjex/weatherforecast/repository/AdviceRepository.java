package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ffanjex.weatherforecast.model.Advice;

public interface AdviceRepository extends JpaRepository<Advice, Integer> {
}
