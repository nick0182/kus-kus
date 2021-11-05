package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskus.service.IngredientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@AllArgsConstructor
public class IngredientController implements IngredientOperations {

    private final IngredientService receiptService;

    @Override
    public Mono<IngredientMatch> searchIngredients(String toSearch, List<String> known) {
        return receiptService.searchIngredients(toSearch, known != null ? known.toArray(String[]::new) : null);
    }
}
