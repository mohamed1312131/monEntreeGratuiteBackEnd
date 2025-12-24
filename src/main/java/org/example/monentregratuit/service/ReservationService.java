package org.example.monentregratuit.service;


import org.example.monentregratuit.DTO.EmailReservationDTO;
import org.example.monentregratuit.DTO.ResRevDTO;
import org.example.monentregratuit.DTO.ReservationsDTO;
import org.example.monentregratuit.entity.Foire;
import org.example.monentregratuit.entity.CountryCode;
import org.example.monentregratuit.entity.ReservationStatus;
import org.example.monentregratuit.repo.FoireRepository;
import org.example.monentregratuit.repo.ReservationsRepository;
import org.springframework.stereotype.Service;
import org.example.monentregratuit.entity.Reservations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ReservationService {

    private final ReservationsRepository reservationRepository;
    private final FoireRepository foireRepository;
    private final EmailTemplateService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.backend.url}")
    private String backendUrl;

    public ReservationService(ReservationsRepository reservationRepository, FoireRepository foireRepository, EmailTemplateService emailService) {
        this.reservationRepository = reservationRepository;
        this.foireRepository = foireRepository;
        this.emailService = emailService;
    }

    /**
     * Fetch all reservations.
     * @return A list of all reservations.
     */
    public List<Reservations> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Fetch latest reservations ordered by reservation date descending.
     * @param limit The maximum number of reservations to return.
     * @return A list of the latest reservations.
     */
    public List<Reservations> getLatestReservations(int limit) {
        return reservationRepository.findAll().stream()
                .sorted((r1, r2) -> r2.getReservationDate().compareTo(r1.getReservationDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Fetch a reservation by its ID.
     * @param id The ID of the reservation.
     * @return The reservation with the given ID.
     * @throws RuntimeException If no reservation is found with the provided ID.
     */
    public Reservations getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
    }


    public Reservations createReservation(ReservationsDTO reservationDTO) {
        Foire foire = foireRepository.findById(reservationDTO.getFoireId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Foire ID: " + reservationDTO.getFoireId()));
        
        // Check for duplicate email for this foire
        if (reservationRepository.existsByFoireIdAndEmail(reservationDTO.getFoireId(), reservationDTO.getEmail())) {
            throw new IllegalArgumentException("Une réservation existe déjà avec cet email pour cette foire");
        }
        
        // Check for duplicate phone for this foire
        if (reservationDTO.getPhone() != null && !reservationDTO.getPhone().isEmpty() 
            && reservationRepository.existsByFoireIdAndPhone(reservationDTO.getFoireId(), reservationDTO.getPhone())) {
            throw new IllegalArgumentException("Une réservation existe déjà avec ce numéro de téléphone pour cette foire");
        }
        
        System.out.println("le foire name est :"+foire.getName());

        Reservations reservation = new Reservations();
        reservation.setFoire(foire);
        reservation.setName(reservationDTO.getName());
        reservation.setCity(reservationDTO.getCity());
        reservation.setEmail(reservationDTO.getEmail());
        reservation.setPhone(reservationDTO.getPhone());
        reservation.setAgeCategory(reservationDTO.getAgeCategory());
        reservation.setCreatedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    /**
     * Update an existing reservation.
     * @param id The ID of the reservation to update.
     * @param updatedReservation The updated reservation data.
     * @return The updated reservation.
     * @throws RuntimeException If no reservation is found with the provided ID.
     */
    public Reservations updateReservation(Long id, Reservations updatedReservation) {
        return reservationRepository.findById(id)
                .map(existingReservation -> {
                    existingReservation.setReservationDate(updatedReservation.getReservationDate());
                    existingReservation.setFoire(updatedReservation.getFoire());
                    existingReservation.setName(updatedReservation.getName());
                    existingReservation.setCity(updatedReservation.getCity());
                    existingReservation.setEmail(updatedReservation.getEmail());
                    existingReservation.setPhone(updatedReservation.getPhone());
                    existingReservation.setAgeCategory(updatedReservation.getAgeCategory());
                    return reservationRepository.save(existingReservation);
                })
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
    }


    /**
     * Delete a reservation by its ID.
     * @param id The ID of the reservation to delete.
     * @throws RuntimeException If no reservation is found with the provided ID.
     */
    public void deleteReservation(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new RuntimeException("Reservation not found with ID: " + id);
        }
        reservationRepository.deleteById(id);
    }
    public List<Object[]> getReservationsByCountryAndYear(int year) {
        return reservationRepository.countReservationsByCountryAndYear(year);
    }
    public List<Reservations> findByYearAndCountry(int year, String country) {
        CountryCode code = CountryCode.valueOf(country);
        return reservationRepository.findByYearAndCountryCode(year, code);
    }
    public long countReservationsByFoireId(Long foireId) {
        return reservationRepository.countByFoireId(foireId);
    }

    public List<ResRevDTO> getAllReservationsDTO() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    private ResRevDTO convertToDTO(Reservations reservation) {
        ResRevDTO dto = new ResRevDTO();
        dto.setId(reservation.getId());
        dto.setFoireId(reservation.getFoire().getId());

        // Since foireDate is stored as a String in the Foire entity, use it directly.
        dto.setFoireDate(reservation.getFoire().getDate());

        dto.setFoireName(reservation.getFoire().getName());
        dto.setName(reservation.getName());
        dto.setCity(reservation.getCity());
        dto.setEmail(reservation.getEmail());
        dto.setPhone(reservation.getPhone());
        dto.setAgeCategory(reservation.getAgeCategory());
        dto.setCountry(reservation.getFoire().getCountryCode() != null ? reservation.getFoire().getCountryCode().name() : null);

        // Format reservationDate (assumed to be a LocalDateTime) using the pattern "dd/MM/yyyy"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dto.setReservationDate(reservation.getReservationDate().format(formatter));

        // Set the status from the reservation (assuming it's of type ReservationStatus)
        dto.setStatus(reservation.getStatus());

        return dto;
    }
    public Reservations setReservationStatus(Long id, ReservationStatus status) {
        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with ID: " + id));
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }
    public List<Reservations> setReservationsStatus(List<Long> ids, ReservationStatus status) {
        List<Reservations> reservations = reservationRepository.findAllById(ids);
        if (reservations.size() != ids.size()) {
            throw new RuntimeException("Some reservations not found");
        }
        reservations.forEach(reservation -> reservation.setStatus(status));
        return reservationRepository.saveAll(reservations);
    }
    public EmailReservationDTO convertToEmailReservationDTO(Reservations reservation) {
        EmailReservationDTO dto = new EmailReservationDTO();
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setAgeCategory(reservation.getAgeCategory());
        dto.setPhone(reservation.getPhone());
        dto.setEmail(reservation.getEmail());
        dto.setCity(reservation.getCity());
        dto.setName(reservation.getName());
        dto.setFoireName(reservation.getFoire().getName());
        dto.setFoireDate(reservation.getFoire().getDate());
        return dto;
    }


    public void sendConfirmationEmail(Reservations reservation) {
        String templateKey = "confirmation-email2"; // Ensure this template exists in the templates directory

        EmailReservationDTO dto = convertToEmailReservationDTO(reservation);

        Map<String, String> variables = new HashMap<>();
        variables.put("Customer Name", dto.getName());
        variables.put("Fair Name", dto.getFoireName());
        variables.put("Reservation ID", reservation.getId().toString());
        variables.put("Date", dto.getReservationDate());
        variables.put("Location", dto.getCity());
        variables.put("Phone", dto.getPhone());
        variables.put("Email", dto.getEmail());
        variables.put("Age Category", dto.getAgeCategory().toString());
        variables.put("Foire Date", dto.getFoireDate().toString());
        variables.put("Confirmation Link", backendUrl + "/api/reservations/confirm/" + reservation.getId());

        emailService.sendBulkEmails(Collections.singletonList(dto.getEmail()), templateKey, variables);
    }

    public void sendConfirmationEmails(List<Long> reservationIds) {
        List<Reservations> reservations = reservationRepository.findAllById(reservationIds);
        for (Reservations reservation : reservations) {
            sendConfirmationEmail(reservation);
        }
    }

    public List<Long> getMonthlyReservations(int year) {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    YearMonth yearMonth = YearMonth.of(year, month);
                    LocalDate start = yearMonth.atDay(1);
                    LocalDate end = yearMonth.atEndOfMonth();
                    return reservationRepository.countByReservationDateBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
                })
                .collect(Collectors.toList());
    }






}
