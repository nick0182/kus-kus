package com.shaidulin.kuskus.controller;

import com.shaidulin.kuskus.dto.ingredient.IngredientMatch;
import com.shaidulin.kuskus.dto.ingredient.IngredientValue;
import com.shaidulin.kuskus.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;

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
                .given(receiptService.searchIngredients(anyString(), any()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/ingredients?toSearch=капуста")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("should return a match for 1 ingredient")
    void test2() {
        IngredientMatch expected = new IngredientMatch(Collections.singleton(new IngredientValue("шампиньоны", 50)));
        BDDMockito
                .given(receiptService.searchIngredients(anyString(), any()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/ingredients?toSearch=шампиньоны")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("should return a match for 2 ingredients")
    void test3() {
        IngredientMatch expected = new IngredientMatch(Collections.singleton(new IngredientValue("чеснок", 45)));
        BDDMockito
                .given(receiptService.searchIngredients(anyString(), notNull()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/ingredients?toSearch=шампиньоны&known=чеснок")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("should return a match for 3 ingredients")
    void test4() {
        IngredientMatch expected = new IngredientMatch(Collections.singleton(new IngredientValue("чеснок", 30)));
        BDDMockito
                .given(receiptService.searchIngredients(anyString(), notNull(), notNull()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/ingredients?toSearch=шампиньоны&known=чеснок&known=соль")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("should not pass known query param to service method")
    void test5() {
        IngredientMatch expected = new IngredientMatch(Collections.singleton(new IngredientValue("чеснок", 15)));
        BDDMockito
                .given(receiptService.searchIngredients(anyString(), any()))
                .willReturn(Mono.just(expected));

        webTestClient
                .get()
                .uri("/api/v1/ingredients?toSearch=шампиньоны&known")
                .exchange()
                .expectStatus().isOk()
                .expectBody(IngredientMatch.class)
                .isEqualTo(expected);
    }
}
