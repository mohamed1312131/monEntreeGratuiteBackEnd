package org.example.monentregratuit.DTO;

import lombok.Data;
import org.example.monentregratuit.entity.AgeCategory;

@Data
public class ReservationsDTO {
    private Long foireId;
    private String name;
    private String city;
    private String email;
    private String phone;
    private AgeCategory ageCategory;

}
