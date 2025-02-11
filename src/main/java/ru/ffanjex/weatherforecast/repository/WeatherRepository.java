package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ffanjex.weatherforecast.model.Weather;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, Integer> {
}
