package org.example.monentregratuit.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.monentregratuit.DTO.EmailReservationDTO;
import org.example.monentregratuit.DTO.FoireDTO;
import org.example.monentregratuit.entity.Foire;
import org.example.monentregratuit.entity.CountryCode;
import org.example.monentregratuit.entity.ReservationStatus;
import org.example.monentregratuit.entity.Reservations;
import org.example.monentregratuit.repo.FoireRepository;
import org.example.monentregratuit.repo.ExcelUserRepository;
import org.example.monentregratuit.repo.ReservationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FoireService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private FoireRepository foireRepository;
    @Autowired
    private ReservationsRepository reservationsRepository;
    
    @Autowired
    private ExcelUserRepository excelUserRepository;

    @Autowired
    private EmailTemplateService emailTemplateService;
    /**
     * Retrieves all fairs (Foires) for a specific country.
     *
     * @param countryCode Country code (e.g., "FR", "CH", "BE").
     * @return List of Foire entities for the specified country.
     */
    public List<FoireDTO> getFoiresByCountry(String countryCode) {
        CountryCode code = CountryCode.valueOf(countryCode);
        return foireRepository.findByCountryCode(code).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private FoireDTO convertToDTO(Foire foire) {
        FoireDTO dto = new FoireDTO();
        dto.setId(foire.getId());
        dto.setName(foire.getName());
        dto.setUrl(foire.getUrl());
        dto.setImage(foire.getImage());
        dto.setDescription(foire.getDescription());
        dto.setLocation(foire.getLocation());
        dto.setDateRanges(foire.getDateRangesList());
        dto.setIsActive(foire.getIsActive());
        dto.setDisponible(foire.getDisponible());
        dto.setCreatedAt(foire.getCreatedAt());
        dto.setUpdatedAt(foire.getUpdatedAt());
        return dto;
    }


    /**
     * Deletes a fair (Foire) by its ID and associated country.
     * Also deletes all related ExcelUsers before deletion.
     *
     * @param countryCode Country code (e.g., "FR", "CH", "BE").
     * @param id          The ID of the fair to delete.
     */
    @Transactional
    public void deleteFoire(String countryCode, Long id) {
        Foire foire = foireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fair ID: " + id));
        if (foire.getCountryCode() != null && foire.getCountryCode().name().equalsIgnoreCase(countryCode)) {
            // Delete all ExcelUsers associated with this foire first
            excelUserRepository.deleteByFoireId(id);
            // Now delete the foire (reservations will be cascade deleted)
            foireRepository.delete(foire);
        } else {
            throw new IllegalArgumentException("Foire does not belong to the given country: " + countryCode);
        }
    }

    /**
     * Adds a new fair (Foire) for a specific country.
     *
     * @param countryCode The country code (e.g., "FR", "CH", "BE").
     * @param name        The name of the fair.
     * @param file        The image file to upload for the fair.
     * @param location    The location of the fair.
     * @param description The description of the fair.
     * @param dateRangesJson JSON string of date ranges.
     * @throws IOException If there is an error uploading the image to Cloudinary.
     */
    public void addFoire(String countryCode, String name, MultipartFile file, String location, String description, String dateRangesJson) throws IOException {
        // Upload image to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("secure_url").toString();

        // Generate a unique reference for the fair
        String reference = "FOIRE" + new Random().nextInt(10000000);

        // Create and save the new Foire
        Foire foire = new Foire();
        foire.setName(name);
        foire.setDescription(description);
        foire.setLocation(location);
        foire.setUrl(reference);
        foire.setImage(imageUrl);
        foire.setDateRanges(dateRangesJson);
        foire.setIsActive(false);
        foire.setCountryCode(CountryCode.valueOf(countryCode));

        foireRepository.save(foire);
    }

    public void updateFoire(String countryCode, Long id, String name, MultipartFile file, String location, String description, String dateRangesJson) throws IOException {
        Foire foire = foireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fair ID: " + id));

        // Check if the fair belongs to the specified country
        if (foire.getCountryCode() != null && foire.getCountryCode().name().equalsIgnoreCase(countryCode)) {
            foire.setName(name);
            foire.setDescription(description);
            foire.setLocation(location);
            foire.setDateRanges(dateRangesJson);

            // Only update image if a new file is provided
            if (file != null && !file.isEmpty()) {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = uploadResult.get("secure_url").toString();
                foire.setImage(imageUrl);
            }

            foireRepository.save(foire);
        } else {
            throw new IllegalArgumentException("Foire does not belong to the given country: " + countryCode);
        }
    }

    /**
     * Activates a fair (Foire) by setting its status to active.
     *
     * @param countryCode The country code (e.g., "FR", "CH", "BE").
     * @param id          The ID of the fair to activate.
     */
    public void activateFoire(String countryCode, long id) {
        Foire foire = foireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fair ID: " + id));

        // Check if the fair belongs to the specified country
        if (foire.getCountryCode() != null && foire.getCountryCode().name().equalsIgnoreCase(countryCode)) {
            foire.setIsActive(true);
            foireRepository.save(foire);
        } else {
            throw new IllegalArgumentException("Foire does not belong to the given country: " + countryCode);
        }
    }

    public List<Foire> getAllFoires(){
        return this.foireRepository.findAll();
    }
    public void disableFoire(String countryCode, long id) {
        Foire foire = foireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fair ID: " + id));

        // Check if the fair belongs to the specified country
        if (foire.getCountryCode() != null && foire.getCountryCode().name().equalsIgnoreCase(countryCode)) {
            foire.setIsActive(false);
            foireRepository.save(foire);
        } else {
            throw new IllegalArgumentException("Foire does not belong to the given country: " + countryCode);
        }
    }

    public void toggleDisponible(String countryCode, long id) {
        Foire foire = foireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fair ID: " + id));

        // Check if the fair belongs to the specified country
        if (foire.getCountryCode() != null && foire.getCountryCode().name().equalsIgnoreCase(countryCode)) {
            foire.setDisponible(!foire.getDisponible());
            foireRepository.save(foire);
        } else {
            throw new IllegalArgumentException("Foire does not belong to the given country: " + countryCode);
        }
    }
    public List<FoireDTO> getActiveFoiresByCountry(String countryCode) {
        CountryCode code = CountryCode.valueOf(countryCode);
        return foireRepository.findByCountryCodeAndIsActiveTrue(code).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void updateExpiredFoires() {
        List<Foire> allFoires = foireRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Foire foire : allFoires) {
            // Check if all date ranges have expired
            List<Foire.DateRange> dateRanges = foire.getDateRangesList();
            if (!dateRanges.isEmpty() && foire.getIsActive()) {
                boolean allExpired = dateRanges.stream().allMatch(range -> {
                    try {
                        LocalDateTime endDate = java.time.LocalDate.parse(range.getEndDate()).atTime(23, 59, 59);
                        return endDate.plusHours(24).isBefore(now);
                    } catch (Exception e) {
                        return false;
                    }
                });
                if (allExpired) {
                    foire.setIsActive(false);
                    foireRepository.save(foire);
                }
            }
        }
    }

    public void sendEmailReminders(Long foireId) {
        List<Reservations> reservations = reservationsRepository.findByFoireId(foireId);
        List<EmailReservationDTO> emailReservationDTOs = reservations.stream()
                .map(this::convertToEmailReservationDTO)
                .collect(Collectors.toList());

        for (EmailReservationDTO dto : emailReservationDTOs) {
            // Format date ranges for email display
            String foireDatesStr = dto.getFoireDateRanges().stream()
                    .map(range -> range.getStartDate() + " - " + range.getEndDate())
                    .collect(java.util.stream.Collectors.joining(", "));
            
            Map<String, String> variables = Map.of(
                    "Customer Name", dto.getName(),
                    "Fair Name", dto.getFoireName(),
                    "Date", dto.getReservationDate(),
                    "Location", dto.getCity(),
                    "Phone", dto.getPhone(),
                    "Email", dto.getEmail(),
                    "Foire Date", foireDatesStr
            );
            emailTemplateService.sendBulkEmails(Collections.singletonList(dto.getEmail()), "reminder_template", variables);
        }
    }

    private EmailReservationDTO convertToEmailReservationDTO(Reservations reservation) {
        EmailReservationDTO dto = new EmailReservationDTO();
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setAgeCategory(reservation.getAgeCategory());
        dto.setPhone(reservation.getPhone());
        dto.setEmail(reservation.getEmail());
        dto.setCity(reservation.getCity());
        dto.setName(reservation.getName());
        dto.setFoireName(reservation.getFoire().getName());
        dto.setFoireDateRanges(reservation.getFoire().getDateRangesList());
        return dto;
    }
    public Map<String, Long> countReservationsByAgeCategory(Long foireId) {
        List<Object[]> results = reservationsRepository.countByFoireIdGroupByAgeCategory(foireId);
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put(result[0].toString(), (Long) result[1]);
        }
        return counts;
    }
    public Map<String, Long> countReservationsByStatus(Long foireId) {
        List<Object[]> results = reservationsRepository.countByFoireIdGroupByStatus(foireId);
        Map<String, Long> counts = new HashMap<>();
        for (Object[] result : results) {
            counts.put(result[0].toString(), (Long) result[1]);
        }
        return counts;
    }
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void updateExpiredReservations() {
        List<Foire> allFoires = foireRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Foire foire : allFoires) {
            // Check if all date ranges have expired
            List<Foire.DateRange> dateRanges = foire.getDateRangesList();
            if (!dateRanges.isEmpty()) {
                boolean allExpired = dateRanges.stream().allMatch(range -> {
                    try {
                        LocalDateTime endDate = java.time.LocalDate.parse(range.getEndDate()).atTime(23, 59, 59);
                        return endDate.plusHours(24).isBefore(now);
                    } catch (Exception e) {
                        return false;
                    }
                });
                if (allExpired) {
                    List<Reservations> reservations = foire.getReservations();
                    for (Reservations reservation : reservations) {
                        if (reservation.getStatus() != ReservationStatus.COMPLETED && reservation.getStatus() != ReservationStatus.BLOCKED) {
                            reservation.setStatus(ReservationStatus.COMPLETED);
                            reservationsRepository.save(reservation);
                        }
                    }
                }
            }
        }
    }






}
