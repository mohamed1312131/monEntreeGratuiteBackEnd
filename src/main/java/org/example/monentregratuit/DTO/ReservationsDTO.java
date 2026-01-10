package org.example.monentregratuit.DTO;

import lombok.Data;
import org.example.monentregratuit.entity.AgeCategory;

@Data
public class ReservationsDTO {
    private Long foireId;
    private String pays;
    private String entreeType;
    private String ville;
    private String nom;
    private String prenom;
    private String smsNumber;
    private String email;
    private String telephone;
    private String trancheAge;
    private AgeCategory ageCategory;
    private String selectedDate;
    private String selectedTime;
    private String recaptchaToken;
    
    // Legacy fields for backward compatibility
    private String name;
    private String city;
    private String phone;
    
    // Getters that map to legacy fields if new fields are null
    public String getName() {
        return nom != null ? nom + " " + (prenom != null ? prenom : "") : name;
    }
    
    public String getCity() {
        return ville != null ? ville : city;
    }
    
    public String getPhone() {
        return telephone != null ? telephone : phone;
    }
}
