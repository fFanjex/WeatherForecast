package ru.ffanjex.weatherforecast.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.service.FactService;

@Controller
@RequiredArgsConstructor
public class FactController {

    private final FactService factService;

    @GetMapping("/interesting-fact")
    public String showAllInterestingFacts(Model model,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "3") int size) {
        try {
            var factPage = factService.findAllFacts(PageRequest.of(page, size));
            model.addAttribute("factsPage", factPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", factPage.getTotalPages());
            return "facts";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Ошибка загрузки фактов: " + e.getMessage());
            return "error";
        }
    }
}