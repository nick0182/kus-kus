package com.shaidulin.kuskus.converter;

import com.shaidulin.kuskus.document.Portion;
import org.springframework.core.convert.converter.Converter;

public class PortionReadingConverter implements Converter<String, Portion> {
    @Override
    public Portion convert(String source) {
        for (Portion portion : Portion.values()) {
            if (portion.name().equals(source)) {
                return portion;
            }
        }
        throw new IllegalStateException("No valid Portion found for source: " + source);
    }
}
