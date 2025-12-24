package org.example.monentregratuit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String reference;

    @Column(name = "slider_order")
    private Integer order;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;
}
