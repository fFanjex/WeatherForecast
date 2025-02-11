package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ffanjex.weatherforecast.model.City;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
}
