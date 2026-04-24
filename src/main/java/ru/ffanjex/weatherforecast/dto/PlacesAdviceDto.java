package ru.ffanjex.weatherforecast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlacesAdviceDto {
    private String name;
    private String description;
    private String reason;
}
