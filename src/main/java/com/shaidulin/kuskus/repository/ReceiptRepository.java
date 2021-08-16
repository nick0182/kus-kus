package com.shaidulin.kuskus.repository;

import com.shaidulin.kuskus.document.Receipt;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ReceiptRepository extends ReactiveElasticsearchRepository<Receipt, String> {
}
