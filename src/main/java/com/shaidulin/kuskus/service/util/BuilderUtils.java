package com.shaidulin.kuskus.service.util;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public record BuilderUtils() {

    public static final String INDEX_NAME = "receipt";
    public static final String PATH = "ingredients";
    public static final String KEYWORD = "ingredients.name.keyword";

    public static BoolQueryBuilder constructIngredientQuery(String... knownIngredients) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String knownIngredient : knownIngredients) {
            String knownIngredientTrimmed = knownIngredient.trim();
            boolQueryBuilder
                    .must(QueryBuilders
                            .nestedQuery(PATH, QueryBuilders.termQuery(KEYWORD, knownIngredientTrimmed), ScoreMode.None));
        }
        return boolQueryBuilder;
    }
}
