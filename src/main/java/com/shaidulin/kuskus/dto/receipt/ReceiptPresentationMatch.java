package com.shaidulin.kuskus.dto.receipt;

import java.util.List;

public record ReceiptPresentationMatch(int total, List<ReceiptPresentationValue> receipts) {}