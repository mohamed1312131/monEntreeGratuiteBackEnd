package org.example.monentregratuit.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_visits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String country;
    private LocalDateTime visitDate = LocalDateTime.now();

}