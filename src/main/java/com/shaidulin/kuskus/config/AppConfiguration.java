package com.shaidulin.kuskus.config;

import com.shaidulin.kuskus.service.IngredientService;
import com.shaidulin.kuskus.service.impl.IngredientServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;

@Configuration
public class AppConfiguration {

    @Bean
    public IngredientService ingredientService(ReactiveElasticsearchClient client) {
        return new IngredientServiceImpl(client);
    }
}
