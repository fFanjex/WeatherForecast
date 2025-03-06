package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ffanjex.weatherforecast.model.Fact;

@Repository
public interface FactRepository extends JpaRepository<Fact, Integer> {
    Page<Fact> findAllByOrderByIdDesc(Pageable pageable);
}
