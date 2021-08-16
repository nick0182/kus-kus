package com.shaidulin.kuskus.document;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
public class Step {

    @Field(type = FieldType.Integer, coerce = false, docValues = false, index = false)
    private final int number;

    @Field(type = FieldType.Text, index = false, norms = false)
    private final String text;
}