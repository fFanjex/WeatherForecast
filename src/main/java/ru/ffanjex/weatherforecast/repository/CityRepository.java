package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ffanjex.weatherforecast.model.City;

public interface CityRepository extends JpaRepository<City, Integer> {
    City findByName(String name);
}
