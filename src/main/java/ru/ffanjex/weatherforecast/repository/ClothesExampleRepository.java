package ru.ffanjex.weatherforecast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ffanjex.weatherforecast.dto.enums.Sex;
import ru.ffanjex.weatherforecast.model.ClothesExample;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClothesExampleRepository extends JpaRepository<ClothesExample, UUID> {

    List<ClothesExample> findBySexAndSeason(Sex sex, String season);

    List<ClothesExample> findBySexAndSeasonAndItemTypeIn(
            Sex sex,
            String season,
            List<String> itemTypes
    );
}