package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.ExpoChartDataDTO;
import org.example.monentregratuit.service.DashboardStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class DashboardStatisticsController {

    @Autowired
    private DashboardStatisticsService dashboardStatisticsService;

    /**
     * Get expo chart data for dashboard
     * GET /api/dashboard/expo-chart
     */
    @GetMapping("/expo-chart")
    public ResponseEntity<ExpoChartDataDTO> getExpoChartData() {
        ExpoChartDataDTO data = dashboardStatisticsService.getExpoChartData();
        return ResponseEntity.ok(data);
    }

    /**
     * Get dashboard counts (total exposants, reservations, etc.)
     * GET /api/dashboard/counts
     */
    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getDashboardCounts() {
        Map<String, Long> counts = dashboardStatisticsService.getDashboardCounts();
        return ResponseEntity.ok(counts);
    }
}
