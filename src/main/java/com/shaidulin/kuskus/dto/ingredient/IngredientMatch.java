package com.shaidulin.kuskus.dto.ingredient;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class IngredientMatch {
    Set<IngredientValue> ingredients;
}
