package ru.ffanjex.weatherforecast.dto;

import lombok.Data;

@Data
public class PlacesAdviceRequest {
    private String city;
    private String country;
    private String temperature;
    private String humidity;
    private String windSpeed;
    private String weatherDescription;
}
