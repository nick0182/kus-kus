package com.shaidulin.kuskus.service.util;

import com.shaidulin.kuskus.dto.receipt.SortType;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.*;

import java.util.Collections;

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

    public static SortBuilder<?> constructSort(SortType sortType) {
        return switch (sortType) {
            case ACCURACY -> SortBuilders.scriptSort(new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG,
                            "params['_source']['ingredients'].size()", Collections.emptyMap()),
                    ScriptSortBuilder.ScriptSortType.NUMBER);
            case COOK_TIME -> SortBuilders.fieldSort("time-to-cook-min");
        };
    }
}
