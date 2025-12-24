package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exposant_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExposantRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_etablissement", nullable = false)
    private String nomEtablissement;

    @Column(name = "secteur_activite", nullable = false)
    private String secteurActivite;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String civilite;

    @Column(name = "nom_prenom", nullable = false)
    private String nomPrenom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(name = "accept_contact", nullable = false)
    private Boolean acceptContact = true;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, CONTACTED, ACCEPTED, REJECTED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
