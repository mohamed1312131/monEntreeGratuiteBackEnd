package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.ResRevDTO;
import org.example.monentregratuit.DTO.ReservationsDTO;
import org.example.monentregratuit.entity.ReservationStatus;
import org.example.monentregratuit.entity.Reservations;
import org.example.monentregratuit.service.ReservationService;
import org.example.monentregratuit.service.UserVisitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationsController {
    private final ReservationService reservationService;
    private final UserVisitService userVisitService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    public ReservationsController(ReservationService reservationService, UserVisitService userVisitService) {
        this.reservationService = reservationService;
        this.userVisitService = userVisitService;
    }

    @GetMapping
    public List<Reservations> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/latest")
    public List<Reservations> getLatestReservations(@RequestParam(defaultValue = "10") int limit) {
        return reservationService.getLatestReservations(limit);
    }

    @GetMapping("/{id}")
    public Reservations getReservationById(@PathVariable long id) {
        return reservationService.getReservationById(id);
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationsDTO reservationDTO) {
        try {
            Reservations createdReservation = reservationService.createReservation(reservationDTO);
            return ResponseEntity.status(201).body(createdReservation);
        } catch (IllegalArgumentException e) {
            // Return 409 Conflict for duplicate reservations
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(409).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public Reservations updateReservation(@PathVariable long id, @RequestBody Reservations reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable long id) {
        reservationService.deleteReservation(id);
    }
    @GetMapping("/stats/{year}")
    public ResponseEntity<List<Map<String, Object>>> getReservationsByCountry(@PathVariable int year) {
        List<Object[]> results = reservationService.getReservationsByCountryAndYear(year);
        List<Map<String, Object>> response = results.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("country", obj[0]);
            map.put("count", obj[1]);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/statsByAge")
    public Map<String, Long> getReservationsByAgeCategory(
            @RequestParam int year,
            @RequestParam String country) {

        List<Reservations> reservations = reservationService.findByYearAndCountry(year, country);

        // Group by AgeCategory and count occurrences
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        res -> res.getAgeCategory().toString(),
                        Collectors.counting()
                ));
    }
    @GetMapping("/count/{foireId}")
    public ResponseEntity<Long> countReservationsByFoireId(@PathVariable Long foireId) {
        long count = reservationService.countReservationsByFoireId(foireId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/GetAll")
    public List<ResRevDTO> getAllReservationsDTO() {
        return reservationService.getAllReservationsDTO();
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<Reservations> updateReservationStatus(@PathVariable Long id, @RequestParam ReservationStatus status) {
        Reservations updatedReservation = reservationService.setReservationStatus(id, status);
        return ResponseEntity.ok(updatedReservation);
    }

    @PutMapping("/status")
    public ResponseEntity<List<Reservations>> updateReservationsStatus(@RequestBody List<Long> ids, @RequestParam ReservationStatus status) {
        List<Reservations> updatedReservations = reservationService.setReservationsStatus(ids, status);
        return ResponseEntity.ok(updatedReservations);
    }
    @PutMapping("/{id}/sendConfirmationEmail")
    public ResponseEntity<Void> sendConfirmationEmail(@PathVariable Long id) {
        Reservations reservation = reservationService.getReservationById(id);
        reservationService.sendConfirmationEmail(reservation);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/confirm/{id}")
    public RedirectView confirmReservation(@PathVariable Long id) {
        reservationService.setReservationStatus(id, ReservationStatus.CONFIRMED);
        return new RedirectView(frontendUrl + "/confirmation-success"); // Redirect to a success page
    }
    @PutMapping("/sendConfirmationEmails")
    public ResponseEntity<Void> sendConfirmationEmails(@RequestBody List<Long> reservationIds) {
        reservationService.sendConfirmationEmails(reservationIds);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/monthly-data")
    public Map<String, Object> getMonthlyData(@RequestParam int year) {
        List<Long> monthlyUserEntries = userVisitService.getMonthlyUserEntries(year);
        List<Long> monthlyReservations = reservationService.getMonthlyReservations(year);

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("monthlyUserEntries", monthlyUserEntries);
        response.put("monthlyReservations", monthlyReservations);

        return response;
    }

}