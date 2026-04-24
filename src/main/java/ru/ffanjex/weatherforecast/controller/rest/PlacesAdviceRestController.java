package ru.ffanjex.weatherforecast.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.dto.PlacesAdviceRequest;
import ru.ffanjex.weatherforecast.dto.PlacesAdviceResponse;
import ru.ffanjex.weatherforecast.service.PlacesAdviceService;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlacesAdviceRestController {

    private final PlacesAdviceService placesAdviceService;

    @PostMapping("/advice")
    public ResponseEntity<PlacesAdviceResponse> generatePlacesAdvice(
            @RequestBody PlacesAdviceRequest request
    ) {
        return ResponseEntity.ok(
                new PlacesAdviceResponse(
                        placesAdviceService.generatePlaces(request)
                )
        );
    }
}