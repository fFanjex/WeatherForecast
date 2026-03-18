package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClothesController {

    @GetMapping("/example-clothes")
    public String exampleClothes(@RequestParam(required = false, defaultValue = "Здесь будет совет по одежде") String advice,
                                 Model model) {
        model.addAttribute("advice", advice);
        return "example-clothes";
    }
}