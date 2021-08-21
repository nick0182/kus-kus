package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.config.ElasticsearchConfig;
import com.shaidulin.kuskus.dto.IngredientMatch;
import com.shaidulin.kuskus.dto.IngredientValue;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
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
    public void setupIndex() throws IOException {
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

    @ParameterizedTest(name = "{index} test \"{0}\"")
    @MethodSource("provideSource")
    void test(String toMatch, IngredientMatch expected) {
        // when
        Mono<IngredientMatch> result = receiptService.searchIngredients(toMatch);

        // then
        result
                .as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();
    }

    private static Stream<Arguments> provideSource() {
        return Stream.of(
                Arguments.of("лук", expectedMatch1()),
                Arguments.of("БО", expectedMatch2()),
                Arguments.of("КаПуС бе", expectedEmptyMatch()),
                Arguments.of("КаПуСта б", expectedMatch3()),
                Arguments.of("  капуста      б  ", expectedMatch3()),
                Arguments.of("Сель", expectedMatch4()),
                Arguments.of("сель, ч", expectedEmptyMatch())
        );
    }

    private static IngredientMatch expectedMatch1() {
        Set<IngredientValue> expectedIngredients = new TreeSet<>(Comparator.comparingInt(IngredientValue::getCount).reversed());
        expectedIngredients.addAll(Set.of(
                new IngredientValue("лук белый", 1),
                new IngredientValue("лук красный", 2),
                new IngredientValue("лук зеленый", 14),
                new IngredientValue("лук репчатый", 97),
                new IngredientValue("лук-порей", 2),
                new IngredientValue("шелуха луковая", 1)
        ));
        return new IngredientMatch(expectedIngredients);
    }

    private static IngredientMatch expectedMatch2() {
        return new IngredientMatch(Collections.singleton(new IngredientValue("перец болгарский", 15)));
    }

    private static IngredientMatch expectedMatch3() {
        Set<IngredientValue> expectedIngredients = new TreeSet<>(Comparator.comparingInt(IngredientValue::getCount).reversed());
        expectedIngredients.addAll(Set.of(
                new IngredientValue("капуста белокочанная", 15),
                new IngredientValue("капуста брюссельская", 2)
        ));
        return new IngredientMatch(expectedIngredients);
    }

    private static IngredientMatch expectedMatch4() {
        Set<IngredientValue> expectedIngredients = new TreeSet<>(Comparator.comparingInt(IngredientValue::getCount).reversed());
        expectedIngredients.addAll(Set.of(
                new IngredientValue("сельдерей черешковый", 6),
                new IngredientValue("сельдерей корневой", 4),
                new IngredientValue("сельдь", 2)
        ));
        return new IngredientMatch(expectedIngredients);
    }

    private static IngredientMatch expectedEmptyMatch() {
        return new IngredientMatch(Collections.emptySet());
    }
}
