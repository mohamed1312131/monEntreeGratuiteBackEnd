package org.example.monentregratuit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a fair (Foire).
 */
@Entity
@Table(name = "foires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Foire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique ID for the fair

    

    @Enumerated(EnumType.STRING)
    @Column(name = "country_code", length = 2)
    private CountryCode countryCode; // redundant but convenient filter (FR/CH/BE)

    @Column(nullable = false)
    private String name; // Name of the fair

    private String url; // URL related to the fair (could be the fair's website)

    private String image; // Image URL or path for the fair

    @Deprecated
    private LocalDateTime date; // Legacy: Date of the fair (kept for backward compatibility)

    @Deprecated
    private LocalDateTime endDate; // Legacy: end date/time of the fair (kept for backward compatibility)

    @Column(name = "date_ranges", columnDefinition = "TEXT")
    private String dateRanges; // JSON array of date ranges [{"startDate": "2026-01-01", "endDate": "2026-01-03"}]

    @Column(columnDefinition = "TEXT")
    private String description; // Description of the fair

    private String location; // Location where event takes place (city, venue, etc.)
    private String city; // city where event takes place
    private String venue; // venue name
    private String address; // street address
    private String postalCode; // postal/zip code

    @Column(nullable = false)
    private Boolean isActive; // Indicates whether the fair is active or not
    
    private Boolean isPublished; // control public visibility
    
    @Column(nullable = true)
    private Boolean disponible = true; // Indicates whether the fair is available for reservations

    private LocalDateTime publishAt; // schedule when to publish

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Timestamp for when the fair was created

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Timestamp for when the fair was last updated
    
    @JsonIgnore
    @OneToMany(mappedBy = "foire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservations> reservations = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods for date ranges
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<DateRange> getDateRangesList() {
        if (dateRanges == null || dateRanges.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dateRanges, new TypeReference<List<DateRange>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    public void setDateRangesList(List<DateRange> ranges) {
        try {
            this.dateRanges = objectMapper.writeValueAsString(ranges);
        } catch (JsonProcessingException e) {
            this.dateRanges = "[]";
        }
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        private String startDate; // Format: yyyy-MM-dd
        private String endDate;   // Format: yyyy-MM-dd
    }
}
