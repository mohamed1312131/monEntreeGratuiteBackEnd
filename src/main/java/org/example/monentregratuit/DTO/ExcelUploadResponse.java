package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadResponse {
    private boolean success;
    private String message;
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<String> errors;
}
