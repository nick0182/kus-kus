package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@RequestMapping("/api/v1/ingredients")
public interface IngredientOperations {

    @GetMapping
    Mono<IngredientMatch> searchIngredients(@RequestParam String toSearch,
                                            @RequestParam(required = false) List<String> known);
}