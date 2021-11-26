package com.shaidulin.kuskus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shaidulin.kuskus.dto.Page;
import com.shaidulin.kuskus.dto.SortType;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
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
                .map(hits -> new ReceiptPresentationMatch(getTotalHits(hits),
                        Arrays.stream(hits.getHits())
                                .map(hit -> convertJson(hit.getSourceAsString()))
                                .collect(Collectors.toList())));
    }

    private int getTotalHits(SearchHits responseHits) {
        return Math.min((int) responseHits.getTotalHits().value, INDEX_MAX_RESULT_WINDOW);
    }

    @SneakyThrows
    private ReceiptPresentationValue convertJson(String source) {
        return objectMapper.readValue(source, ReceiptPresentationValue.class);
    }
}
