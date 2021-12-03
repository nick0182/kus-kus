package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.controller.receipt.ReceiptController;
import com.shaidulin.kuskus.dto.Page;
import com.shaidulin.kuskus.dto.SortType;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationValue;
import com.shaidulin.kuskus.service.ReceiptService;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

@WebFluxTest(ReceiptController.class)
public class ReceiptControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReceiptService receiptService;

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
        ReceiptPresentationMatch expected = new ReceiptPresentationMatch(false, Collections.emptyList());
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
                new ReceiptPresentationMatch(false, Collections.singletonList(
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
