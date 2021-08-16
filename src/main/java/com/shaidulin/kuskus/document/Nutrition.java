package com.shaidulin.kuskus.document;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
public class Nutrition {

    @Field(name = "name", type = FieldType.Keyword, ignoreAbove = 30, index = false)
    private final Portion portion;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10, coerce = false, index = false)
    private final double calories;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10, coerce = false, index = false)
    private final double protein;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10, coerce = false, index = false)
    private final double fat;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 10, coerce = false, index = false)
    private final double carbohydrate;
}