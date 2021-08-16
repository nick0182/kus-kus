package com.shaidulin.kuskus.converter;

import org.springframework.core.convert.converter.Converter;

import java.time.Duration;

public class DurationReadingConverter implements Converter<String, Duration> {
    @Override
    public Duration convert(String source) {
        return Duration.parse(source);
    }
}
