package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExampleClothesController {
    @GetMapping("/example-clothes")
    public String showExampleClothesPage() {
        return "example-clothes";
    }
}