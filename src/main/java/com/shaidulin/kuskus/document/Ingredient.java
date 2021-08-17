package com.shaidulin.kuskus.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@AllArgsConstructor
public class Ingredient {

    @MultiField(
            mainField = @Field(type = FieldType.Text, index = false, norms = false),
            otherFields = {
                    @InnerField(suffix = "search", type = FieldType.Search_As_You_Type),
                    @InnerField(
                            suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 40,
                            normalizer = "ingredients_name_keyword_normalizer", index = false
                    )
            }
    )
    private final String name;

    @Field(type = FieldType.Integer, coerce = false)
    private final Integer quantity;

    @Field(type = FieldType.Keyword, ignoreAbove = 10, normalizer = "ingredients_measurement_keyword_normalizer", index = false)
    private final String measurement;
}