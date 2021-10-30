package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.config.ElasticsearchConfig;
import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskus.dto.ingredient.IngredientValue;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@SpringBootTest(classes = ElasticsearchConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReceiptServiceTest {

    private static final String INDEX_NAME = "receipt";

    private static final XContentType NDJSON = XContentType.fromMediaType("application/x-ndjson");

    private static final int DOCUMENTS_COUNT = 290;

    @Autowired
    private ReactiveElasticsearchClient client;

    @Value("classpath:/recipe-bulk.jsonl")
    private Resource testData;

    @Autowired
    private ReceiptService receiptService;

    @BeforeAll
    public void setupIndex() throws IOException, InterruptedException {
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

        Thread.sleep(1000);
    }

    private void populateIndex() throws IOException {
        // given
        BulkRequest request = new BulkRequest();
        byte[] requestBodyBytes = testData.getInputStream().readAllBytes();
        request.add(new BytesArray(requestBodyBytes), INDEX_NAME, NDJSON);

        // when
        Mono<BulkResponse> result = client.bulk(request);

        // then
        result
                .as(StepVerifier::create)
                .expectNextMatches(bulkResult -> bulkResult.status().equals(RestStatus.OK)
                        && bulkResult.getItems().length == DOCUMENTS_COUNT)
                .verifyComplete();
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SearchFirstIngredient {
        @ParameterizedTest(name = "{index} search \"{0}\"")
        @MethodSource("provideSource")
        void test(String toSearch, IngredientMatch expected) {
            // when
            Mono<IngredientMatch> result = receiptService.searchIngredients(toSearch);

            // then
            result
                    .as(StepVerifier::create)
                    .expectNext(expected)
                    .verifyComplete();
        }

        private Stream<Arguments> provideSource() {
            return Stream.of(
                    Arguments.of("лук",
                            expectedMatch(
                                    new IngredientValue("лук белый", 1),
                                    new IngredientValue("лук красный", 2),
                                    new IngredientValue("лук зеленый", 14),
                                    new IngredientValue("лук репчатый", 97),
                                    new IngredientValue("лук-порей", 2),
                                    new IngredientValue("шелуха луковая", 1)
                            )
                    ),

                    Arguments.of("БО",
                            expectedMatch(
                                    new IngredientValue("перец болгарский", 15)
                            )
                    ),

                    Arguments.of("КаПуС бе",
                            expectedEmptyMatch()
                    ),

                    Arguments.of("КаПуСта б",
                            expectedMatch(
                                    new IngredientValue("капуста белокочанная", 15),
                                    new IngredientValue("капуста брюссельская", 2)
                            )
                    ),

                    Arguments.of("  капуста      б  ",
                            expectedMatch(
                                    new IngredientValue("капуста белокочанная", 15),
                                    new IngredientValue("капуста брюссельская", 2)
                            )
                    ),

                    Arguments.of("Сель",
                            expectedMatch(
                                    new IngredientValue("сельдерей черешковый", 6),
                                    new IngredientValue("сельдерей корневой", 4),
                                    new IngredientValue("сельдь", 2)
                            )
                    ),

                    Arguments.of("сель, ч",
                            expectedEmptyMatch()
                    )
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SearchSecondIngredient {
        @ParameterizedTest(name = "{index} search \"{0}\" with known \"{1}\"")
        @MethodSource("provideSource")
        void test(String toSearch, String known, IngredientMatch expected) {
            // when
            Mono<IngredientMatch> result = receiptService.searchIngredients(toSearch, known);

            // then
            result
                    .as(StepVerifier::create)
                    .expectNext(expected)
                    .verifyComplete();
        }

        private Stream<Arguments> provideSource() {
            return Stream.of(
                    Arguments.of("кур", "чеснок",
                            expectedMatch(
                                    new IngredientValue("яйцо куриное", 13),
                                    new IngredientValue("курица", 4),
                                    new IngredientValue("грудка куриная", 2),
                                    new IngredientValue("бедро куриное", 1)
                            )
                    ),

                    Arguments.of("ба", "чеснок",
                            expectedMatch(
                                    new IngredientValue("баклажан", 7),
                                    new IngredientValue("базилик", 1),
                                    new IngredientValue("баранина", 1),
                                    new IngredientValue("батон", 1)
                            )
                    ),

                    Arguments.of("  бА  ", "чеснок",
                            expectedMatch(
                                    new IngredientValue("баклажан", 7),
                                    new IngredientValue("базилик", 1),
                                    new IngredientValue("баранина", 1),
                                    new IngredientValue("батон", 1)
                            )
                    ),

                    Arguments.of("масло по", "соль",
                            expectedMatch(
                                    new IngredientValue("масло подсолнечное", 5)
                            )
                    ),

                    Arguments.of("масло, гренки", "соль",
                            expectedEmptyMatch()
                    ),

                    Arguments.of("пер", "перец черный",
                            expectedMatch(
                                    new IngredientValue("перец болгарский", 3),
                                    new IngredientValue("перец красный жгучий", 1),
                                    new IngredientValue("перец сладкий", 1),
                                    new IngredientValue("перец сладкий красный", 1)
                            )
                    ),

                    Arguments.of("ПЕр", "перец черный",
                            expectedMatch(
                                    new IngredientValue("перец болгарский", 3),
                                    new IngredientValue("перец красный жгучий", 1),
                                    new IngredientValue("перец сладкий", 1),
                                    new IngredientValue("перец сладкий красный", 1)
                            )
                    ),

                    Arguments.of("Яй", "яйцо куриное",
                            expectedMatch(
                                    new IngredientValue("яйцо перепелиное", 1)
                            )
                    )
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SearchThirdIngredient {
        @ParameterizedTest(name = "{index} search \"{0}\" with known \"{1}\" and \"{2}\"")
        @MethodSource("provideSource")
        void test(String toSearch, String known1, String known2, IngredientMatch expected) {
            // when
            Mono<IngredientMatch> result = receiptService.searchIngredients(toSearch, known1, known2);

            // then
            result
                    .as(StepVerifier::create)
                    .expectNext(expected)
                    .verifyComplete();
        }

        private Stream<Arguments> provideSource() {
            return Stream.of(
                    Arguments.of("масло", "соль", "перец черный",
                            expectedMatch(
                                    new IngredientValue("масло растительное", 24),
                                    new IngredientValue("масло сливочное", 11),
                                    new IngredientValue("масло подсолнечное", 1)
                            )
                    ),

                    Arguments.of("ба", "чеснок", "укроп",
                            expectedMatch(
                                    new IngredientValue("базилик", 1),
                                    new IngredientValue("баранина", 1)
                            )
                    ),

                    Arguments.of("  бА  ", "чеснок", "укроп",
                            expectedMatch(
                                    new IngredientValue("базилик", 1),
                                    new IngredientValue("баранина", 1)
                            )
                    ),

                    Arguments.of("масло по", "соль", "чеснок",
                            expectedMatch(
                                    new IngredientValue("масло подсолнечное", 3)
                            )
                    ),

                    Arguments.of("масло, гренки", "соль", "чеснок",
                            expectedEmptyMatch()
                    ),

                    Arguments.of("ба", "базилик", "укроп",
                            expectedMatch(
                                    new IngredientValue("баранина", 1)
                            )
                    ),

                    Arguments.of("бА   ", "базилик", "укроп",
                            expectedMatch(
                                    new IngredientValue("баранина", 1)
                            )
                    )
            );
        }
    }

    private IngredientMatch expectedMatch(IngredientValue... matches) {
        Set<IngredientValue> expectedIngredients = new TreeSet<>(Set.of(matches));
        return new IngredientMatch(expectedIngredients);
    }

    private IngredientMatch expectedEmptyMatch() {
        return new IngredientMatch(Collections.emptySet());
    }
}
