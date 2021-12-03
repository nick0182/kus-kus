package com.shaidulin.kuskus.dto.receipt;

import java.util.List;

public record ReceiptPresentationMatch(boolean hasMore, List<ReceiptPresentationValue> receipts) {}