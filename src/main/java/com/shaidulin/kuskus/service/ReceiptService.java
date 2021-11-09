package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import reactor.core.publisher.Mono;

public interface ReceiptService {
    Mono<ReceiptPresentationMatch> getReceiptRepresentations(String... ingredients);
}
