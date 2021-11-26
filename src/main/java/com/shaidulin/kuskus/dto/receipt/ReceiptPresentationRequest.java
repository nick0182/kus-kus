package com.shaidulin.kuskus.dto.receipt;

import com.shaidulin.kuskus.dto.Page;
import com.shaidulin.kuskus.dto.SortType;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReceiptPresentationRequest {
    @NotEmpty(message = "At least 1 ingredient should be present")
    List<String> ingredients;
    @NotNull
    SortType sortType;
    @NotNull
    Page page;
}