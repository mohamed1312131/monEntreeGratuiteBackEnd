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
    private String ipAddress;
    private String interests;
    private Boolean phoneContactConsent;
    private Boolean partnerDataSharingConsent;
    private Boolean marketingConsent;
    private Boolean termsAccepted;
    private String consentCapturedAt;
    private String termsVersion;
    private String privacyPolicyVersion;
    private String selectedDate;
    private String selectedTime;
    private String reservationDate;
    private AgeCategory ageCategory;
    private ReservationStatus status;
    private String country;
    private String createdAt;
}
