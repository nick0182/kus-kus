package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.controller.receipt.ReceiptController;
import com.shaidulin.kuskus.document.Portion;
import com.shaidulin.kuskus.dto.receipt.*;
import com.shaidulin.kuskus.service.ReceiptService;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@WebFluxTest(ReceiptController.class)
public class ReceiptControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReceiptService receiptService;

    @Nested
    class ReceiptPresentationsTest {
        @Test
        @DisplayName("should not pass with null request param validation")
        void test1() {
            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations")
                    .exchange()
                    .expectStatus().isBadRequest();

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients")
                    .exchange()
                    .expectStatus().isBadRequest();

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients=лук,сыр")
                    .exchange()
                    .expectStatus().isBadRequest();

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients=масло,сыр?sortType=ACCURACY")
                    .exchange()
                    .expectStatus().isBadRequest();

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients=масло,сыр?sortType=ACCURACY&page.current=0")
                    .exchange()
                    .expectStatus().isBadRequest();

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients=масло,сыр?sortType=ACCURACY&page.current=0&page.size")
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("should not pass with empty request param validation")
        void test2() {
            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?sortType=ACCURACY&page.current=0&page.size=10&ingredients=")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(String.class)
                    .value(StringContains.containsString("At least 1 ingredient should be present"));
        }

        @Test
        @DisplayName("should return no match")
        void test3() {
            ReceiptPresentationMatch expected = new ReceiptPresentationMatch(new Meta(SortType.ACCURACY, 0, false), Collections.emptyList());
            BDDMockito
                    .given(receiptService.getReceiptRepresentations(SortType.ACCURACY, new Page(0, 10), "капуста"))
                    .willReturn(Mono.just(expected));

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients=капуста&sortType=ACCURACY&page.current=0&page.size=10")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ReceiptPresentationMatch.class)
                    .isEqualTo(expected);
        }

        @Test
        @DisplayName("should return a match")
        void test4() {
            ReceiptPresentationMatch expected =
                    new ReceiptPresentationMatch(new Meta(SortType.ACCURACY, 0, false), Collections.singletonList(
                            new ReceiptPresentationValue(45, "Оладьи", Duration.ofMinutes(30), 2)));
            BDDMockito
                    .given(receiptService.getReceiptRepresentations(SortType.ACCURACY, new Page(0, 10), "молоко", "мука"))
                    .willReturn(Mono.just(expected));

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/presentations?ingredients=молоко&ingredients=мука&" +
                            "sortType=ACCURACY&page.current=0&page.size=10")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ReceiptPresentationMatch.class)
                    .isEqualTo(expected);
        }
    }

    @Nested
    class ReceiptByIdTest {

        @Test
        @DisplayName("should return 404 when not found")
        void test1() {
            BDDMockito
                    .given(receiptService.getReceipt(110))
                    .willReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/110")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should return receipt when found")
        void test2() {
            List<Ingredient> ingredients = Collections.singletonList(new Ingredient("Соль", 1, "упак."));
            List<Nutrition> nutritions = Collections.singletonList(
                    new Nutrition(Portion.HUNDRED_GRAMS, 16.2, 4.4, 8.6, 12.0));
            ReceiptValue expected = new ReceiptValue(221, "Селедка под шубой",
                    Duration.ofHours(1), 2, ingredients, nutritions);

            BDDMockito
                    .given(receiptService.getReceipt(221))
                    .willReturn(Mono.just(expected));

            webTestClient
                    .get()
                    .uri("/api/v1/receipts/221")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ReceiptValue.class)
                    .isEqualTo(expected);
        }
    }
}