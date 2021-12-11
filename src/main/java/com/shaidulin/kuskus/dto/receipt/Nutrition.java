package com.shaidulin.kuskus.dto.receipt;

import com.shaidulin.kuskus.document.Portion;

public record Nutrition(Portion name, double calories, double protein, double fat, double carbohydrate) {}