package com.shaidulin.kuskus.controller.receipt;

import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@RequestMapping("/api/v1/receipts")
@Validated
public interface ReceiptOperations {

    @GetMapping("/presentations")
    Mono<ReceiptPresentationMatch> getReceiptPresentations(
            @RequestParam @NotEmpty(message = "At least 1 ingredient should be present") List<String> ingredients);
}
