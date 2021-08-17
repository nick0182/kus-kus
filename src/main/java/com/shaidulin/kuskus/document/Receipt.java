package com.shaidulin.kuskus.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Document(indexName = "receipt")
@DynamicMapping(value = DynamicMappingValue.False)
@Setting(settingPath = "index_settings.json")
@Data
@AllArgsConstructor
public class Receipt {

    @Field(name = "query-param", type = FieldType.Integer, index = false, coerce = false, docValues = false)
    private final int queryParam;

    @Field(type = FieldType.Text, analyzer = "russian", norms = false)
    private final String name;

    @Field(type = FieldType.Text, index = false, norms = false)
    private final String description;

    @Field(type = FieldType.Nested)
    private final List<Ingredient> ingredients;

    @Field(name = "time-to-cook", type = FieldType.Keyword, ignoreAbove = 20, index = false)
    private final Duration cookTime;

    @Field(type = FieldType.Integer, coerce = false)
    private final int portions;

    @Field(name = "nutritional-energy-value", type = FieldType.Object)
    private final List<Nutrition> nutritions;

    @Field(name = "steps-todo", type = FieldType.Object)
    private final List<Step> steps;

    @Field(type = FieldType.Keyword, ignoreAbove = 100, index = false)
    private final Set<String> categories;
}