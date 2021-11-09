package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.controller.receipt.ReceiptController;
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

import static org.mockito.ArgumentMatchers.anyString;

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
                .uri("/api/v1/receipts/presentations?ingredients")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("should not pass with empty request param validation")
    void test2() {
        webTestClient
                .get()
                .uri("/api/v1/receipts/presentations?ingredients=")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(StringContains.containsString("At least 1 ingredient should be present"));
    }

    @Test
    @DisplayName("should return no match")
    void test3() {
        ReceiptPresentationMatch expected = new ReceiptPresentationMatch(Collections.emptyList());
        BDDMockito
                .given(receiptService.getReceiptRepresentations(anyString()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/receipts/presentations?ingredients=капуста")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReceiptPresentationMatch.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("should return a match")
    void test4() {
        ReceiptPresentationMatch expected =
                new ReceiptPresentationMatch(Collections.singletonList(
                        new ReceiptPresentationValue(45, "Оладьи", Duration.ofMinutes(30), 2)));
        BDDMockito
                .given(receiptService.getReceiptRepresentations(anyString(), anyString()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/receipts/presentations?ingredients=молоко&ingredients=мука")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReceiptPresentationMatch.class)
                .isEqualTo(expected);
    }
}
