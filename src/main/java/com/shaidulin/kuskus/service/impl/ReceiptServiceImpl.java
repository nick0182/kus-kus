package com.shaidulin.kuskus.service.impl;

import com.shaidulin.kuskus.dto.IngredientMatch;
import com.shaidulin.kuskus.dto.IngredientValue;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.bucket.SingleBucketAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.shaidulin.kuskus.service.impl.ReceiptServiceImpl.IngredientSourceBuilder.constructKnownSource;
import static com.shaidulin.kuskus.service.impl.ReceiptServiceImpl.IngredientSourceBuilder.constructSearchSource;

@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReactiveElasticsearchClient client;

    @Override
    public Mono<IngredientMatch> searchIngredients(String toSearch, String... known) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().size(0);
        if (known != null) {
            sourceBuilder.query(constructKnownSource(known));
        }
        sourceBuilder.aggregation(constructSearchSource(toSearch, known));
        SearchRequest request = new SearchRequest("receipt").source(sourceBuilder);

        return client.searchForResponse(request)
                .map(response -> IngredientSourceBuilder.resolveAggregation(response.getAggregations(), "ingredients", Nested.class))
                .map(this::createSearchResponse);
    }

    private IngredientMatch createSearchResponse(Terms termsAgg) {
        return new IngredientMatch(
                termsAgg.getBuckets()
                        .stream()
                        .map(bucket -> new IngredientValue(bucket.getKeyAsString(), (int) bucket.getDocCount()))
                        .collect(Collectors.toCollection(TreeSet::new))
        );
    }

    static class IngredientSourceBuilder {

        private static final String SEARCH_PLAIN = "ingredients.name.search"; // splits phrase by word
        private static final String SEARCH_2GRAM = "ingredients.name.search._2gram"; // splits phrase by 2 words
        private static final String SEARCH_3GRAM = "ingredients.name.search._3gram"; // splits phrase by 3 words

        private static final String PATH = "ingredients";
        private static final String KEYWORD = "ingredients.name.keyword";

        static NestedAggregationBuilder constructSearchSource(String toSearch, String... known) {
            String toMatchTrimmed = toSearch.trim();
            MultiMatchQueryBuilder multiMatchQuery = constructMultiMatchBuilder(toMatchTrimmed);
            FilterAggregationBuilder filterIngredientsAggregation = constructFilterAggregationBuilder(multiMatchQuery);
            TermsAggregationBuilder ingredientsListAggregation = constructTermsAggregationBuilder(known);
            return packAggregations(ingredientsListAggregation, filterIngredientsAggregation);
        }

        static BoolQueryBuilder constructKnownSource(String... known) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            for (String knownIngredient : known) {
                String knownIngredientTrimmed = knownIngredient.trim();
                boolQueryBuilder
                        .must(QueryBuilders
                                .nestedQuery(PATH, QueryBuilders.termQuery(KEYWORD, knownIngredientTrimmed), ScoreMode.None));
            }
            return boolQueryBuilder;
        }

        static <T extends Aggregation> Terms resolveAggregation(Aggregations parent, String name, Class<T> type) {
            if (type.equals(Nested.class)) {
                return resolveAggregation(((SingleBucketAggregation) parent.get(name)).getAggregations(), "filter_ingredients", Filter.class);
            } else if (type.equals(Filter.class)) {
                return resolveAggregation(((SingleBucketAggregation) parent.get(name)).getAggregations(), "ingredients_list", Terms.class);
            } else {
                return parent.get(name);
            }
        }

        private static MultiMatchQueryBuilder constructMultiMatchBuilder(String toMatchTrimmed) {
            MultiMatchQueryBuilder multiMatchQueryBuilder;
            boolean isSingleWord = !StringUtils.containsWhitespace(toMatchTrimmed);
            if (isSingleWord) {
                multiMatchQueryBuilder = new MultiMatchQueryBuilder(toMatchTrimmed, SEARCH_PLAIN, SEARCH_2GRAM, SEARCH_3GRAM);
            } else {
                multiMatchQueryBuilder = new MultiMatchQueryBuilder(toMatchTrimmed, SEARCH_2GRAM, SEARCH_3GRAM);
            }
            multiMatchQueryBuilder.type(MultiMatchQueryBuilder.Type.BOOL_PREFIX);
            return multiMatchQueryBuilder;
        }

        private static FilterAggregationBuilder constructFilterAggregationBuilder(MultiMatchQueryBuilder multiMatchQuery) {
            return new FilterAggregationBuilder("filter_ingredients", multiMatchQuery);
        }

        private static TermsAggregationBuilder constructTermsAggregationBuilder(String... known) {
            TermsAggregationBuilder termsAggregationBuilder =
                    AggregationBuilders
                            .terms("ingredients_list")
                            .field(KEYWORD);

            if (known != null) {
                termsAggregationBuilder.includeExclude(new IncludeExclude(null, known));
            }
            return termsAggregationBuilder;
        }

        private static NestedAggregationBuilder packAggregations(TermsAggregationBuilder ingredientsListAggregation,
                                                                 FilterAggregationBuilder filterIngredientsAggregation) {
            filterIngredientsAggregation.subAggregations(AggregatorFactories.builder().addAggregator(ingredientsListAggregation));
            NestedAggregationBuilder nestedAggregation = new NestedAggregationBuilder(PATH, PATH);
            nestedAggregation.subAggregations(AggregatorFactories.builder().addAggregator(filterIngredientsAggregation));
            return nestedAggregation;
        }
    }
}
