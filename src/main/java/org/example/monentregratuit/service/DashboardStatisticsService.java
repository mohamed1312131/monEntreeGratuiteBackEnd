package org.example.monentregratuit.service;

import org.example.monentregratuit.DTO.ExpoChartDataDTO;
import org.example.monentregratuit.entity.ReservationStatus;
import org.example.monentregratuit.repo.ExposantRequestRepository;
import org.example.monentregratuit.repo.ReservationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardStatisticsService {

    @Autowired
    private ExposantRequestRepository exposantRequestRepository;

    @Autowired
    private ReservationsRepository reservationsRepository;

    /**
     * Get expo chart data grouped by country, gender, and sector
     * For now, we'll use mock data structure since ExposantRequest doesn't have country/gender fields
     * In a real scenario, you would add these fields to the entity
     */
    public ExpoChartDataDTO getExpoChartData() {
        // Get all accepted exposant requests
        var acceptedRequests = exposantRequestRepository.findByStatus("ACCEPTED");
        
        // Countries
        List<String> countries = Arrays.asList("France", "Belgique", "Suisse");
        
        // For now, generate statistics based on actual data count
        // In production, you would extract country and gender from the entity
        int totalAccepted = acceptedRequests.size();
        
        // Distribute data across countries and categories
        // This is a simplified distribution - in production, use actual data
        List<ExpoChartDataDTO.SeriesData> series = new ArrayList<>();
        
        // Male - Moniteur
        series.add(new ExpoChartDataDTO.SeriesData(
            "Male - Moniteur",
            Arrays.asList(
                Math.max(1, totalAccepted / 6),
                Math.max(1, totalAccepted / 5),
                Math.max(1, totalAccepted / 8)
            )
        ));
        
        // Female - Moniteur
        series.add(new ExpoChartDataDTO.SeriesData(
            "Female - Moniteur",
            Arrays.asList(
                Math.max(1, totalAccepted / 8),
                Math.max(1, totalAccepted / 7),
                Math.max(1, totalAccepted / 9)
            )
        ));
        
        // Male - Coach
        series.add(new ExpoChartDataDTO.SeriesData(
            "Male - Coach",
            Arrays.asList(
                Math.max(1, totalAccepted / 10),
                Math.max(1, totalAccepted / 8),
                Math.max(1, totalAccepted / 6)
            )
        ));
        
        // Female - Coach
        series.add(new ExpoChartDataDTO.SeriesData(
            "Female - Coach",
            Arrays.asList(
                Math.max(1, totalAccepted / 12),
                Math.max(1, totalAccepted / 10),
                Math.max(1, totalAccepted / 9)
            )
        ));
        
        return new ExpoChartDataDTO(countries, series);
    }

    /**
     * Get total counts for dashboard cards
     */
    public Map<String, Long> getDashboardCounts() {
        Map<String, Long> counts = new HashMap<>();
        
        // Exposant requests counts
        counts.put("totalExposants", exposantRequestRepository.count());
        counts.put("pendingExposants", (long) exposantRequestRepository.findByStatus("PENDING").size());
        counts.put("acceptedExposants", (long) exposantRequestRepository.findByStatus("ACCEPTED").size());
        
        // Reservation counts
        counts.put("totalReservations", reservationsRepository.count());
        counts.put("pendingReservations", reservationsRepository.countByStatus(ReservationStatus.PENDING));
        counts.put("confirmedReservations", reservationsRepository.countByStatus(ReservationStatus.CONFIRMED));
        
        return counts;
    }
}
