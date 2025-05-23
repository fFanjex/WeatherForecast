package ru.ffanjex.weatherforecast.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.model.Fact;
import ru.ffanjex.weatherforecast.service.FactService;

@RestController
@RequestMapping("/api/facts")
@RequiredArgsConstructor
public class FactRestController {

    private final FactService factService;

    @GetMapping
    public ResponseEntity<Page<Fact>> getFacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        Page<Fact> factPage = factService.findAllFacts(PageRequest.of(page, size));
        return ResponseEntity.ok(factPage);
    }
}
