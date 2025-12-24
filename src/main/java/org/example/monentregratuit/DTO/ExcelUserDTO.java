package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUserDTO {
    private Long id;
    private String nom;
    private String email;
    private String date;
    private String heure;
    private String code;
    private Long foireId;
    private String foireName;
    private LocalDateTime createdAt;
}
