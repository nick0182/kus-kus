package com.shaidulin.kuskus;

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
    public ElasticsearchCustomConversions elasticsearchCustomConversions(
            Converter<String, Duration> durationReadingConverter,
            Converter<String, Portion> portionReadingConverter) {
        return new ElasticsearchCustomConversions(List.of(durationReadingConverter, portionReadingConverter));
    }

    @Bean
    public Converter<String, Duration> durationReadingConverter() {
        return new DurationReadingConverter();
    }

    @Bean
    public Converter<String, Portion> portionReadingConverter() {
        return new PortionReadingConverter();
    }
}