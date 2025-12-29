package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.FoireDTO;
import org.example.monentregratuit.entity.Foire;
import org.example.monentregratuit.service.FoireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foires")
public class FoireController {

    @Autowired
    private FoireService foireService;

    /**
     * Retrieves all fairs (Foires) for a specific country.
     * @param countryCode Country code (e.g., "FR", "CH", "BE").
     * @return List of Foire entities for the specified country.
     */
    @GetMapping("/{countryCode}")
    public List<FoireDTO> getFoiresByCountry(@PathVariable String countryCode) {
        return foireService.getFoiresByCountry(countryCode);
    }

    /**
     * Activates a fair (Foire) by setting its status to active.
     * @param countryCode Country code (e.g., "FR", "CH", "BE").
     * @param id The ID of the fair to activate.
     */
    @PutMapping("/activate/{countryCode}/{id}")
    public void activateFoire(@PathVariable String countryCode, @PathVariable Long id) {
        foireService.activateFoire(countryCode, id);
    }

    /**
     * Deletes a fair (Foire) by its ID and associated country.
     * @param countryCode Country code (e.g., "FR", "CH", "BE").
     * @param id The ID of the fair to delete.
     */
    @DeleteMapping("/{countryCode}/{id}")
    public ResponseEntity<?> deleteFoire(@PathVariable String countryCode, @PathVariable Long id) {
        try {
            foireService.deleteFoire(countryCode, id);
            return ResponseEntity.ok().body(Map.of("message", "Foire deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete foire: " + e.getMessage()));
        }
    }

    /**
     * Adds a new fair (Foire) for a specific country.
     * @param countryCode The country code (e.g., "FR", "CH", "BE").
     * @param name The name of the fair.
     * @param file The image file for the fair.
     * @param location The location of the fair.
     * @param description The description of the fair.
     * @param dateRanges JSON string of date ranges.
     * @throws IOException If there is an error uploading the image.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addFoire(
            @RequestParam String countryCode, 
            @RequestParam String name, 
            @RequestParam MultipartFile file, 
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam String dateRanges) {
        try {
            foireService.addFoire(countryCode, name, file, location, description, dateRanges);
            return ResponseEntity.ok().body(Map.of("message", "Foire added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to add foire: " + e.getMessage()));
        }
    }
    @GetMapping("/all")
    public List<Foire> getAllFoires() {
        return foireService.getAllFoires();
    }
    @PutMapping("/disable/{countryCode}/{id}")
    public void disableFoire(@PathVariable String countryCode, @PathVariable Long id) {
        foireService.disableFoire(countryCode, id);
    }
    @GetMapping("/getAllActive/{countryCode}")
    public List<FoireDTO> getActiveFoiresByCountry(@PathVariable String countryCode) {
        return foireService.getActiveFoiresByCountry(countryCode);
    }
    @PostMapping("/sendReminders/{foireId}")
    public void sendEmailReminders(@PathVariable Long foireId) {
        foireService.sendEmailReminders(foireId);
    }
    @GetMapping("/countReservations/{foireId}")
    public Map<String, Long> countReservationsByAgeCategory(@PathVariable Long foireId) {
        return foireService.countReservationsByAgeCategory(foireId);
    }
    @GetMapping("/countReservationsByStatus/{foireId}")
    public Map<String, Long> countReservationsByStatus(@PathVariable Long foireId) {
        return foireService.countReservationsByStatus(foireId);
    }

    @PutMapping("/toggleDisponible/{countryCode}/{id}")
    public ResponseEntity<?> toggleDisponible(@PathVariable String countryCode, @PathVariable Long id) {
        try {
            foireService.toggleDisponible(countryCode, id);
            return ResponseEntity.ok().body(Map.of("message", "Disponible status toggled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to toggle disponible status: " + e.getMessage()));
        }
    }
}
