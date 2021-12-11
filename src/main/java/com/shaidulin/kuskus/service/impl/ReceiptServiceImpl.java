package com.shaidulin.kuskus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskus.dto.receipt.*;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.shaidulin.kuskus.service.util.BuilderUtils.*;

public record ReceiptServiceImpl(ReactiveElasticsearchClient client,
                                 ObjectMapper objectMapper) implements ReceiptService {

    private static final int INDEX_MAX_RESULT_WINDOW = 10000;

    @Override
    @SneakyThrows
    public Mono<ReceiptPresentationMatch> getReceiptRepresentations(SortType sortType, Page page, String... ingredients) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .from(page.getCurrent())
                .size(page.getSize())
                .fetchSource(new String[]{"query-param", "name", "time-to-cook", "portions"}, null)
                .query(constructIngredientQuery(ingredients))
                .sort(constructSortScript(sortType));

        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);

        return client.searchForResponse(request)
                .map(SearchResponse::getHits)
                .map(hits -> new ReceiptPresentationMatch(createMeta((int) hits.getTotalHits().value, page, sortType),
                        Arrays.stream(hits.getHits())
                                .map(hit -> convertJson(hit.getSourceAsString(), ReceiptPresentationValue.class))
                                .collect(Collectors.toList())));
    }

    @Override
    public Mono<ReceiptValue> getReceipt(int id) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.termQuery("query-param", id))
                .fetchSource(null, new String[]{"description", "steps-todo", "categories"});

        SearchRequest request = new SearchRequest(INDEX_NAME).source(sourceBuilder);

        return client
                .search(request)
                .next()
                .map(hit -> convertJson(hit.getSourceAsString(), ReceiptValue.class))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    private Meta createMeta(int total, Page page, SortType sortType) {
        return new Meta(sortType, page.getCurrent() / page.getSize(),
                Math.min(total, INDEX_MAX_RESULT_WINDOW) > page.getCurrent() + page.getSize());
    }

    @SneakyThrows
    private <T> T convertJson(String source, Class<T> clazz) {
        return objectMapper.readValue(source, clazz);
    }
}
