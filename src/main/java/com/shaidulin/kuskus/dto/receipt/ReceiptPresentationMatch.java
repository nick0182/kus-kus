package com.shaidulin.kuskus.dto.receipt;

import java.util.List;

public record ReceiptPresentationMatch(Meta meta, List<ReceiptPresentationValue> receipts) {}