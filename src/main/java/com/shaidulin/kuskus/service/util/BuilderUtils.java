package com.shaidulin.kuskus.service.util;

import com.shaidulin.kuskus.dto.receipt.SortType;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;

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

    public static ScriptSortBuilder constructSortScript(SortType sortType) {
        return SortBuilders.scriptSort(new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG,
                        mapSortTypeToScriptCode(sortType), Collections.emptyMap()),
                ScriptSortBuilder.ScriptSortType.NUMBER);
    }

    private static String mapSortTypeToScriptCode(SortType sortType) {
        return switch (sortType) {
            case ACCURACY -> "params['_source']['ingredients'].size()";
        };
    }
}
