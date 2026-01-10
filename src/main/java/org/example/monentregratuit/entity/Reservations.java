package org.example.monentregratuit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Entity representing a reservation made for a fair.
 */
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique reservation ID

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foire_id", nullable = false)
    private Foire foire; // Foire selected for the reservation

    @Column(nullable = false)
    private String name; // Name of the person making the reservation

    private String city; // City of the person making the reservation

    private String email; // Email for contact purposes

    private String phone; // Phone number for contact

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgeCategory ageCategory; // Instead of raw age numbers

    @Column(name = "selected_date")
    private String selectedDate; // The specific date selected by user (yyyy-MM-dd format)

    @Column(name = "selected_time")
    private String selectedTime; // The specific time slot selected by user (HH:mm format)

    @Column(name = "reservation_date")
    private LocalDateTime reservationDate; // Date when the reservation is made

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Timestamp for when the reservation was created

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Timestamp for when the reservation was last updated

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status; // Status of the reservation


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        reservationDate = LocalDateTime.now(); // Assuming the reservation date is the creation time
        status = ReservationStatus.PENDING;  // Default status when a reservation is made
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
