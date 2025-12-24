package org.example.monentregratuit.DTO;

import lombok.Data;
import org.example.monentregratuit.entity.AgeCategory;
import org.example.monentregratuit.entity.ReservationStatus; // Make sure this import is correct

import java.time.LocalDateTime;

@Data
public class ResRevDTO {
    private Long id;
    private Long foireId;
    private LocalDateTime foireDate;
    private String foireName;
    private String name;
    private String city;
    private String email;
    private String phone;
    private String reservationDate;
    private AgeCategory ageCategory;
    private ReservationStatus status; // New field for reservation status
    private String country;
}
