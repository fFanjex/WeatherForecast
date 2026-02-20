package ru.ffanjex.weatherforecast.dto;

import lombok.Data;
import ru.ffanjex.weatherforecast.dto.enums.Sex;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private Sex sex;
}
