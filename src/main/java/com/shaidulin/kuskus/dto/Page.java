package com.shaidulin.kuskus.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page {
    int current;
    int size;
}