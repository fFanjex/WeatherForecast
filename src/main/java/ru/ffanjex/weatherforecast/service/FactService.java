package ru.ffanjex.weatherforecast.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.model.Fact;
import ru.ffanjex.weatherforecast.repository.FactRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class FactService {
    private final FactRepository factRepository;

    public Fact save(Fact fact) {
        return factRepository.save(fact);
    }

    public List<Fact> getAllFacts() {
        return factRepository.findAll();
    }
}
