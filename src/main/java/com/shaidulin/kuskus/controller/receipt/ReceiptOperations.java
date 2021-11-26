package com.shaidulin.kuskus.controller.receipt;

import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@RequestMapping("/api/v1/receipts")
public interface ReceiptOperations {

    @GetMapping("/presentations")
    Mono<ReceiptPresentationMatch> getReceiptPresentations(@Validated ReceiptPresentationRequest request);
}
