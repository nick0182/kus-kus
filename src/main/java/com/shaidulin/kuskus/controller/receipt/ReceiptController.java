package com.shaidulin.kuskus.controller.receipt;

import com.shaidulin.kuskus.dto.receipt.ReceiptPresentationMatch;
import com.shaidulin.kuskus.service.ReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@AllArgsConstructor
public class ReceiptController implements ReceiptOperations {

    private final ReceiptService receiptService;

    @Override
    public Mono<ReceiptPresentationMatch> getReceiptPresentations(List<String> ingredients) {
        return receiptService.getReceiptRepresentations(ingredients.toArray(String[]::new));
    }
}
