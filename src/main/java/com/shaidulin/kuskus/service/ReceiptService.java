package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import reactor.core.publisher.Mono;

public interface ReceiptService {
    Mono<IngredientMatch> searchIngredients(String toSearch, String... known);
}
