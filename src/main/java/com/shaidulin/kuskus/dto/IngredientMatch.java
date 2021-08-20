package com.shaidulin.kuskus.dto;

import lombok.Value;

import java.util.Set;

@Value
public class IngredientMatch {
    Set<IngredientValue> ingredients;
}
