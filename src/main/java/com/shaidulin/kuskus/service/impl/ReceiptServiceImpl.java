package com.shaidulin.kuskus.service.impl;

import com.shaidulin.kuskus.dto.IngredientMatch;
import com.shaidulin.kuskus.dto.IngredientValue;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReactiveElasticsearchClient client;

    private static final String SEARCH_PLAIN = "ingredients.name.search";
    private static final String SEARCH_2GRAM = "ingredients.name.search._2gram";
    private static final String SEARCH_3GRAM = "ingredients.name.search._3gram";

    private static final String KEYWORD = "ingredients.name.keyword";

    @Override
    public Mono<IngredientMatch> searchIngredients(String toMatch) {
        MultiMatchQueryBuilder multiMatchQuery = constructMultiMatchBuilder(toMatch);
        FilterAggregationBuilder filterIngredientsAggregation = new FilterAggregationBuilder("filter_ingredients", multiMatchQuery);
        TermsAggregationBuilder ingredientsListAggregation = new TermsAggregationBuilder("ingredients_list");
        ingredientsListAggregation.field(KEYWORD);
        NestedAggregationBuilder nestedAggregation = new NestedAggregationBuilder("ingredients", "ingredients");
        nestedAggregation.subAggregations(
                AggregatorFactories.builder()
                        .addAggregator(filterIngredientsAggregation)
                        .addAggregator(ingredientsListAggregation));
        SearchRequest request = new SearchRequest().source(new SearchSourceBuilder().size(0).aggregation(nestedAggregation));

        return client.searchForResponse(request)
                .map(response -> resolveAggregation(response.getAggregations(), "ingredients", Nested.class))
                .map(this::createSearchResponse);
    }

    private MultiMatchQueryBuilder constructMultiMatchBuilder(String query) {
        MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder(query, SEARCH_PLAIN, SEARCH_2GRAM, SEARCH_3GRAM);
        multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.BOOL_PREFIX);
        return multiMatchQueryBuilder;
    }

    private <T extends Aggregation> Terms resolveAggregation(Aggregations parent, String name, Class<T> type) {
        if (type.equals(Nested.class)) {
            return resolveAggregation(((SingleBucketAggregation) parent.get(name)).getAggregations(), "filter_ingredients", Filter.class);
        } else if (type.equals(Filter.class)) {
            return resolveAggregation(((SingleBucketAggregation) parent.get(name)).getAggregations(), "ingredients_list", Terms.class);
        } else {
            return parent.get(name);
        }
    }

    private IngredientMatch createSearchResponse(Terms termsAgg) {
        return new IngredientMatch(
                termsAgg.getBuckets()
                        .stream()
                        .map(bucket -> new IngredientValue(bucket.getKeyAsString(), (int) bucket.getDocCount()))
                        .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(IngredientValue::getCount).reversed())))
        );
    }
}
