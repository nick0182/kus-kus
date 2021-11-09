package com.shaidulin.kuskus.service.config;

import lombok.SneakyThrows;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ElasticsearchConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ElasticServiceTest {

    private static final String INDEX_NAME = "receipt";

    private static final XContentType NDJSON = XContentType.fromMediaType("application/x-ndjson");

    private static final int DOCUMENTS_COUNT = 290;

    @Autowired
    private ReactiveElasticsearchClient client;

    @Value("classpath:/recipe-bulk.jsonl")
    private Resource testData;

    @BeforeAll
    public void setupIndex() {
        // given
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX_NAME);

        // when
        Mono<Boolean> result = client.indices().existsIndex(getIndexRequest);

        // then
        result
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        populateIndex();

        waitRequestFinish();
    }

    @SneakyThrows
    private void populateIndex() {
        client
                .count(new SearchRequest(INDEX_NAME))
                .filter(count -> count == 0)
                .map(ignored -> getBulkRequest())
                .flatMap(request -> client.bulk(request))
                .doOnNext(this::assertIndexPopulated)
                .subscribe();
    }

    @SneakyThrows
    private BulkRequest getBulkRequest() {
        BulkRequest request = new BulkRequest();
        byte[] requestBodyBytes = testData.getInputStream().readAllBytes();
        request.add(new BytesArray(requestBodyBytes), INDEX_NAME, NDJSON);
        return request;
    }

    private void assertIndexPopulated(BulkResponse bulkResult) {
        assertTrue(bulkResult.status().equals(RestStatus.OK) && bulkResult.getItems().length == DOCUMENTS_COUNT);
    }

    @SneakyThrows
    private void waitRequestFinish() {
        Thread.sleep(1000);
    }
}
