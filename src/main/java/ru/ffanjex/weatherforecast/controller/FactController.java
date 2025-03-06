package ru.ffanjex.weatherforecast.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.ffanjex.weatherforecast.model.Fact;
import ru.ffanjex.weatherforecast.service.FactService;

import java.util.List;

@Controller
@AllArgsConstructor
public class FactController {
    @Autowired
    private final FactService factService;

    @GetMapping("/interesting-fact")
    public String showAllInterestingFacts(Model model) {
        List<Fact> facts = factService.getAllFacts();
        model.addAttribute("facts", facts);
        return "facts";
    }
}
