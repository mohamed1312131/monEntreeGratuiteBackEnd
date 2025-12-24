package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_template_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private EmailTemplate emailTemplate;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String imageName;

    @Column(nullable = false)
    @Builder.Default
    private Integer imageOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
