package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@AllArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/api/v1/ingredients")
    public Mono<IngredientMatch> searchIngredients(@RequestParam String toSearch,
                                                   @RequestParam(required = false) List<String> known) {
        return receiptService.searchIngredients(toSearch, known != null ? known.toArray(String[]::new) : null);
    }
}
