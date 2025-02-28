package ru.ffanjex.weatherforecast.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.model.City;
import ru.ffanjex.weatherforecast.repository.CityRepository;

@Service
@AllArgsConstructor
public class CityService {
    private final CityRepository cityRepository;

    public City findCityById(Integer id) {
        return cityRepository.findById(id).orElse(null);
    }

    public City findByName(String name) {
        return cityRepository.findByName(name);
    }

    public City save(City city) {
        return cityRepository.save(city);
    }
}
