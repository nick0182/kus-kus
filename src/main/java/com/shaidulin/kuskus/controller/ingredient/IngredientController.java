package com.shaidulin.kuskus.controller.ingredient;

import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskus.service.IngredientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@AllArgsConstructor
public class IngredientController implements IngredientOperations {

    private final IngredientService ingredientService;

    @Override
    public Mono<IngredientMatch> searchIngredients(String toSearch, List<String> known) {
        return ingredientService.searchIngredients(toSearch, known != null ? known.toArray(String[]::new) : null);
    }
}
