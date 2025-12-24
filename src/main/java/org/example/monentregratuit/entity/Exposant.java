package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exposants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exposant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime datesub;
    private String nometab;
    private String activite;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer genre;
    private String prenom;
    private String nom;
    private String email;
    private String phone;

    // New fields to support exposant/sponsor request workflow
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType type; // EXPOSANT or SPONSOR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foire_id")
    private Foire foire; // optional: requested fair

    private String companyWebsite;
    private String logoUrl;
    private String city;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        if (datesub == null) {
            datesub = now;
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
        if (type == null) {
            type = RequestType.EXPOSANT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
