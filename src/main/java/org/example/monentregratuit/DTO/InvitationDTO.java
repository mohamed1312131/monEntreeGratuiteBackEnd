package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationDTO {
    private Long id;
    private String nom;
    private String email;
    private String date;
    private String heure;
    private String code;
    private Long foireId;
    private String foireName;
    private Boolean emailSent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
