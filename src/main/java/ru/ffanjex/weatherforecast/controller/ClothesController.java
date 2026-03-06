package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClothesController {
    @GetMapping("/example-clothes")
    public String exampleClothes() {
        return "example-clothes";
    }
}
