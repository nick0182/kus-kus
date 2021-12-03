package com.shaidulin.kuskus.service;

import com.shaidulin.kuskus.dto.receipt.Page;
import com.shaidulin.kuskus.dto.receipt.SortType;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import reactor.core.publisher.Mono;

public interface ReceiptService {
    Mono<ReceiptPresentationMatch> getReceiptRepresentations(SortType sortType, Page page, String... ingredients);
}
