package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "track")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime tm;
    private String site;
    private String country;

    @Column(name = "country_code")
    private String countryCode;

    private String state;
    private String city;
    private String address;
    private String ref;
    private String agent;
    private String ip;

    @Column(name = "ip_value")
    private Integer ipValue;

    private String domain;

    @Column(name = "host_name")
    private String hostName;

    @Column(name = "tracking_page_name")
    private String trackingPageName;
}
