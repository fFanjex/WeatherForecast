package ru.ffanjex.weatherforecast.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClothingItemImageDto {
    private String clothingItem;
    private String prompt;
    private String image;
}