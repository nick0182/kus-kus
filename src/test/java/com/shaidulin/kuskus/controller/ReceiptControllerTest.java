package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.dto.IngredientMatch;
import com.shaidulin.kuskus.dto.IngredientValue;
import com.shaidulin.kuskus.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;

public class ReceiptControllerTest {

    private WebTestClient webTestClient;
    private ReceiptService receiptService;

    @BeforeEach
    void setUp() {
        receiptService = Mockito.mock(ReceiptService.class);
        ReceiptController receiptController = new ReceiptController(receiptService);
        webTestClient = WebTestClient.bindToController(receiptController).build();
    }

    @Test
    @DisplayName("should return no match")
    void test1() {
        IngredientMatch expected = new IngredientMatch(Collections.emptySet());
        BDDMockito
                .given(receiptService.searchIngredients(anyString()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/vi/ingredients/капуста")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("should return a match")
    void test2() {
        IngredientMatch expected = new IngredientMatch(Collections.singleton(new IngredientValue("шампиньоны", 50)));
        BDDMockito
                .given(receiptService.searchIngredients(anyString()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/vi/ingredients/шампиньоны")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }
}
