package org.example.monentregratuit.DTO;

import lombok.Data;
import org.example.monentregratuit.entity.AgeCategory;

import java.time.LocalDateTime;

@Data
public class EmailReservationDTO {
    private String reservationDate;
    private AgeCategory ageCategory;
    private String phone;
    private String email;
    private String city;
    private String name;
    private String foireName;
    private LocalDateTime foireDate;
}