package ru.ffanjex.weatherforecast.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.dto.enums.Sex;
import ru.ffanjex.weatherforecast.model.ClothesExample;
import ru.ffanjex.weatherforecast.service.ClothesExampleService;

import java.util.List;

@RestController
@RequestMapping("/api/clothes")
@AllArgsConstructor
public class ClothesExampleRestController {

    private final ClothesExampleService clothesExampleService;

    @GetMapping
    public ResponseEntity<List<ClothesExample>> getClothesExamples(
            @RequestParam Sex sex,
            @RequestParam String season,
            @RequestParam String advice
    ) {

        List<ClothesExample> examples =
                clothesExampleService.getExamplesByAdvice(sex, season, advice);

        return ResponseEntity.ok(examples);
    }
}