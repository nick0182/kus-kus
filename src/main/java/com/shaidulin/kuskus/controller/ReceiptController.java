package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.dto.IngredientMatch;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/api/vi/ingredients/{ingredientMatch}")
    public Mono<IngredientMatch> searchIngredients(@PathVariable("ingredientMatch") String toMatch) {
        return receiptService.searchIngredients(toMatch);
    }
}
