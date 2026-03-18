package ru.ffanjex.weatherforecast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdviceResponseDto {
    private String adviceText;
    private String shortClothingDescription;
    private String imagePrompt;
}
