package com.shaidulin.kuskus.config;

import com.shaidulin.kuskus.service.ReceiptService;
import com.shaidulin.kuskus.service.impl.ReceiptServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;

@Configuration
public class AppConfiguration {

    @Bean
    public ReceiptService receiptService(ReactiveElasticsearchClient client) {
        return new ReceiptServiceImpl(client);
    }
}
