package ru.ffanjex.weatherforecast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PlacesAdviceResponse {
    List<PlacesAdviceDto> places;
}
