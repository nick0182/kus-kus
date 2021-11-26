package com.shaidulin.kuskus.controller.receipt;

import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationRequest;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class ReceiptController implements ReceiptOperations {

    private final ReceiptService receiptService;

    @Override
    public Mono<ReceiptPresentationMatch> getReceiptPresentations(ReceiptPresentationRequest request) {
        return receiptService.getReceiptRepresentations(request.getSortType(),
                request.getPage(), request.getIngredients().toArray(String[]::new));
    }
}
