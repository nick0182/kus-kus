package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.dto.Page;
import com.shaidulin.kuskus.dto.SortType;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskus.service.config.ElasticServiceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

public class ReceiptServiceTest extends ElasticServiceTest {

    @Autowired
    private ReceiptService receiptService;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ReceiptRepresentationTest {
        @ParameterizedTest(name = "{index} get receipt representations")
        @MethodSource("provideSource")
        void test(ReceiptPresentationMatch expected, String[] ingredients) {
            // when
            Mono<ReceiptPresentationMatch> result = receiptService.getReceiptRepresentations(
                    SortType.ACCURACY, new Page(0, 10), ingredients);

            // then
            result
                    .as(StepVerifier::create)
                    .expectNext(expected)
                    .verifyComplete();
        }

        private Stream<Arguments> provideSource() {
            return Stream.of(
                    Arguments.of(
                            expectedRPM(1, expectedRPV(4688, "Бутерброды \"Объедение\"", Duration.ofMinutes(30), 0)),
                            new String[]{"перец черный", "батон"}
                    ),
                    Arguments.of(
                            expectedRPM(1, expectedRPV(4952, "Рулет из фарша с яйцами", Duration.ofMinutes(75), 4)),
                            new String[]{"соль", "хлеб"}
                    ),
                    Arguments.of(
                            expectedRPM(2,
                                    expectedRPV(4853, "Салат из свеклы", Duration.ofMinutes(10), 4),
                                    expectedRPV(4778, "Салат \"Огни Парижа\"", null, 0)
                            ),
                            new String[]{"свекла"}
                    ),

                    Arguments.of(
                            expectedRPM(2,
                                    expectedRPV(4937, "Курочка в аэрогриле", Duration.ofMinutes(1), 0),
                                    expectedRPV(4857, "Курочка \"По-королевски\"", null, 0)
                            ),
                            new String[]{"масло растительное", "соль", "курица"}
                    ),
                    Arguments.of(
                            expectedRPM(0),
                            new String[]{"банан", "молоко"}
                    )
            );
        }

        private ReceiptPresentationMatch expectedRPM(int total, ReceiptPresentationValue... values) {
            return new ReceiptPresentationMatch(total, Arrays.asList(values));
        }

        private ReceiptPresentationValue expectedRPV(int queryParam, String name, Duration cookTime, int portions) {
            return new ReceiptPresentationValue(queryParam, name, cookTime, portions);
        }
    }

}
