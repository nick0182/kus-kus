package com.shaidulin.kuskus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskus.service.IngredientService;
import com.shaidulin.kuskus.service.ReceiptService;
import com.shaidulin.kuskus.service.impl.IngredientServiceImpl;
import com.shaidulin.kuskus.service.impl.ReceiptServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;

@Configuration
public class AppConfiguration {

    @Bean
    IngredientService ingredientService(ReactiveElasticsearchClient client) {
        return new IngredientServiceImpl(client);
    }

    @Bean
    ReceiptService receiptService(ReactiveElasticsearchClient client, ObjectMapper objectMapper) {
        return new ReceiptServiceImpl(client, objectMapper);
    }
}
