package org.example.monentregratuit.DTO;

import lombok.Data;
import org.example.monentregratuit.entity.AgeCategory;
import org.example.monentregratuit.entity.ReservationStatus;
import org.example.monentregratuit.entity.Foire;

import java.util.List;

@Data
public class ResRevDTO {
    private Long id;
    private Long foireId;
    private List<Foire.DateRange> foireDateRanges;
    private String foireName;
    private String name;
    private String city;
    private String email;
    private String phone;
    private String reservationDate;
    private AgeCategory ageCategory;
    private ReservationStatus status;
    private String country;
    private String createdAt;
}
