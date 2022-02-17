package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.document.Portion;
import com.shaidulin.kuskus.dto.receipt.*;
import com.shaidulin.kuskus.service.config.ElasticServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReceiptServiceTest extends ElasticServiceTest {

    @Autowired
    private ReceiptService receiptService;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ReceiptPresentationTest {
        @ParameterizedTest(name = "{index} get receipt representations")
        @MethodSource("provideSource")
        void test(int currentPage, ReceiptPresentationMatch expected, String[] ingredients) {
            // when
            Mono<ReceiptPresentationMatch> result = receiptService.getReceiptRepresentations(
                    SortType.ACCURACY, new Page(currentPage, 1), ingredients);

            // then
            result
                    .as(StepVerifier::create)
                    .expectNext(expected)
                    .verifyComplete();
        }

        private Stream<Arguments> provideSource() {
            return Stream.of(
                    Arguments.of(
                            0,
                            expectedRPM(new Meta(SortType.ACCURACY, 0, false),
                                    expectedRPV(4688, "Бутерброды \"Объедение\"", Duration.ofMinutes(30), 0)),
                            new String[]{"перец черный", "батон"}
                    ),
                    Arguments.of(
                            1,
                            expectedRPM(new Meta(SortType.ACCURACY, 1, false)),
                            new String[]{"перец черный", "батон"}
                    ),
                    Arguments.of(
                            0,
                            expectedRPM(new Meta(SortType.ACCURACY, 0, false),
                                    expectedRPV(4952, "Рулет из фарша с яйцами", Duration.ofMinutes(75), 4)),
                            new String[]{"соль", "хлеб"}
                    ),
                    Arguments.of(
                            1,
                            expectedRPM(new Meta(SortType.ACCURACY, 1, false)),
                            new String[]{"соль", "хлеб"}
                    ),
                    Arguments.of(
                            0,
                            expectedRPM(new Meta(SortType.ACCURACY, 0, true),
                                    expectedRPV(4853, "Салат из свеклы", Duration.ofMinutes(10), 4)),
                            new String[]{"свекла"}
                    ),
                    Arguments.of(
                            1,
                            expectedRPM(new Meta(SortType.ACCURACY, 1, false),
                                    expectedRPV(4778, "Салат \"Огни Парижа\"", null, 0)),
                            new String[]{"свекла"}
                    ),
                    Arguments.of(
                            0,
                            expectedRPM(new Meta(SortType.ACCURACY, 0, true),
                                    expectedRPV(4937, "Курочка в аэрогриле", Duration.ofMinutes(1), 0)),
                            new String[]{"масло растительное", "соль", "курица"}
                    ),
                    Arguments.of(
                            1,
                            expectedRPM(new Meta(SortType.ACCURACY, 1, false),
                                    expectedRPV(4857, "Курочка \"По-королевски\"", null, 0)),
                            new String[]{"масло растительное", "соль", "курица"}
                    ),
                    Arguments.of(
                            0,
                            expectedRPM(new Meta(SortType.ACCURACY, 0, false)),
                            new String[]{"банан", "молоко"}
                    ),
                    Arguments.of(
                            1,
                            expectedRPM(new Meta(SortType.ACCURACY, 1, false)),
                            new String[]{"банан", "молоко"}
                    )
            );
        }

        private ReceiptPresentationMatch expectedRPM(Meta meta, ReceiptPresentationValue... values) {
            return new ReceiptPresentationMatch(meta, Arrays.asList(values));
        }

        private ReceiptPresentationValue expectedRPV(int queryParam, String name, Duration cookTime, int portions) {
            return new ReceiptPresentationValue(queryParam, name, cookTime, portions);
        }
    }

    @Nested
    class ReceiptByIdTest {

        @Test
        @DisplayName("Should yield RequestStatusException 404 when receipt by id not exists")
        void test1() {
            // expect
            receiptService.getReceipt(4928)
                    .as(StepVerifier::create)
                    .expectErrorMatches(error -> error instanceof ResponseStatusException
                            && ((ResponseStatusException) error).getStatus().equals(HttpStatus.NOT_FOUND))
                    .verify();
        }

        @Test
        @DisplayName("Should yield receipt when receipt by id exists")
        void test2() {
            // given
            List<Ingredient> ingredients = List.of(
                    new Ingredient("Ветчина", 300, "г"),
                    new Ingredient("Помидор", 4, "шт"),
                    new Ingredient("Брынза", 200, "г"),
                    new Ingredient("Грибы", 1, "бан."),
                    new Ingredient("Сметана", null, null),
                    new Ingredient("Майонез", null, null)
            );
            List<Nutrition> nutritions = List.of(
                    new Nutrition(Portion.ALL, 1477.0, 87.1, 114.6, 23.4),
                    new Nutrition(Portion.HUNDRED_GRAMS, 125.2, 7.4, 9.7, 2.0)
            );
            List<Step> steps = List.of(
                    new Step(1, "Режем брынзу и ветчину кубиками."),
                    new Step(2, "Помидоры режем и откладываем на сито, чтобы стек сок."),
                    new Step(3, "Режем грибочки (я покупаю сразу резаные)."),
                    new Step(4, "Майонез и сметану смешиваем в равных пропорциях."),
                    new Step(5, "Заправляем салатик.")
            );
            ReceiptValue expected = new ReceiptValue(4927, "Салат с брынзой и грибами",
                    Duration.ofMinutes(20), 0, ingredients, nutritions, steps);

            // when
            Mono<ReceiptValue> result = receiptService.getReceipt(4927);

            // then
            result
                    .as(StepVerifier::create)
                    .expectNext(expected)
                    .verifyComplete();
        }
    }
}
