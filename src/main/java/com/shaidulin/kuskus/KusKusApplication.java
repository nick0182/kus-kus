package com.shaidulin.kuskus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shaidulin.kuskus.converter.DurationReadingConverter;
import com.shaidulin.kuskus.converter.PortionReadingConverter;
import com.shaidulin.kuskus.document.Portion;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.time.Duration;
import java.util.List;

@SpringBootApplication
public class KusKusApplication {

    public static void main(String[] args) {
        SpringApplication.run(KusKusApplication.class, args);
    }

    @Bean
    ElasticsearchCustomConversions elasticsearchCustomConversions(
            Converter<String, Duration> durationReadingConverter,
            Converter<String, Portion> portionReadingConverter) {
        return new ElasticsearchCustomConversions(List.of(durationReadingConverter, portionReadingConverter));
    }

    @Bean
    Converter<String, Duration> durationReadingConverter() {
        return new DurationReadingConverter();
    }

    @Bean
    Converter<String, Portion> portionReadingConverter() {
        return new PortionReadingConverter();
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        return objectMapper;
    }
}