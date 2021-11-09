package com.shaidulin.kuskus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static com.shaidulin.kuskus.service.util.BuilderUtils.INDEX_NAME;
import static com.shaidulin.kuskus.service.util.BuilderUtils.constructIngredientQuery;

public record ReceiptServiceImpl(ReactiveElasticsearchClient client, ObjectMapper objectMapper) implements ReceiptService {

    private static final int RECEIPTS_COUNT = 10;

    @Override
    @SneakyThrows
    public Mono<ReceiptPresentationMatch> getReceiptRepresentations(String... ingredients) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .size(RECEIPTS_COUNT)
                .fetchSource(new String[]{"query-param", "name", "time-to-cook", "portions"}, null)
                .query(constructIngredientQuery(ingredients));

        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);

        return client.search(request)
                .map(hit -> convertJson(hit.getSourceAsString()))
                .collect(Collectors.toList())
                .map(ReceiptPresentationMatch::new);
    }

    @SneakyThrows
    private ReceiptPresentationValue convertJson(String source) {
        return objectMapper.readValue(source, ReceiptPresentationValue.class);
    }
}
