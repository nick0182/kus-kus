package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.config.ElasticsearchConfig;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.test.StepVerifier;

@SpringBootTest(classes = ElasticsearchConfig.class)
public class ReceiptServiceTest {

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private ReactiveElasticsearchClient client;

    @Value("${recipe-bulk.jsonl}")
    private Resource testData;

    @Test
    void test() {
        GetIndexRequest getIndexRequest = new GetIndexRequest("receipt");
        client.indices().existsIndex(getIndexRequest)
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }
}
