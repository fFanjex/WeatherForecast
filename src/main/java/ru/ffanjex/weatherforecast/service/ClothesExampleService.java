package ru.ffanjex.weatherforecast.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.dto.enums.Sex;
import ru.ffanjex.weatherforecast.model.ClothesExample;
import ru.ffanjex.weatherforecast.repository.ClothesExampleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ClothesExampleService {

    private final ClothesExampleRepository clothesExampleRepository;

    public List<ClothesExample> getExamplesByAdvice(Sex sex, String season, String advice) {

        List<String> itemTypes = extractItemTypes(advice);

        if (itemTypes.isEmpty()) {
            return clothesExampleRepository.findBySexAndSeason(sex, season);
        }

        return clothesExampleRepository.findBySexAndSeasonAndItemTypeIn(
                sex,
                season,
                itemTypes
        );
    }

    private List<String> extractItemTypes(String advice) {

        String text = advice.toLowerCase();
        List<String> items = new ArrayList<>();

        if (text.contains("термобель")) items.add("THERMAL");
        if (text.contains("флис")) items.add("FLEECE");
        if (text.contains("пуховик")) items.add("PUFFER");
        if (text.contains("мембран")) items.add("MEMBRANE");

        if (text.contains("штаны")) items.add("PANTS");
        if (text.contains("брюк")) items.add("PANTS");

        if (text.contains("носки")) items.add("SOCKS");

        if (text.contains("ботин")) items.add("BOOTS");
        if (text.contains("обув")) items.add("SHOES");

        if (text.contains("шапк")) items.add("HAT");
        if (text.contains("шарф")) items.add("SCARF");
        if (text.contains("перчат")) items.add("GLOVES");

        return items;
    }
}