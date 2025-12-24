package org.example.monentregratuit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.example.monentregratuit.entity.UserVisit;
import org.example.monentregratuit.repo.UserVisitRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserVisitService {
    private final UserVisitRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    public UserVisitService(UserVisitRepository repository) {
        this.repository = repository;
    }

    public void trackVisit(String ip) {
        if ("127.0.0.1".equals(ip) || "::1".equals(ip)) {
            ip = getPublicIp(); // Get external IP if local
        }

        String apiUrl = "https://ipapi.co/" + ip + "/json/";
        Map response = restTemplate.getForObject(apiUrl, Map.class);
        String country = response != null ? (String) response.get("country_name") : "Unknown";

        UserVisit visit = new UserVisit();
        visit.setIpAddress(ip);
        visit.setCountry(country);
        repository.save(visit);
    }

    private String getPublicIp() {
        try {
            return restTemplate.getForObject("https://api64.ipify.org?format=json", Map.class).get("ip").toString();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    public List<Long> getMonthlyUserEntries(int year) {
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    YearMonth yearMonth = YearMonth.of(year, month);
                    LocalDate start = yearMonth.atDay(1);
                    LocalDate end = yearMonth.atEndOfMonth();
                    return repository.countByVisitDateBetween(start.atStartOfDay(), end.atTime(23, 59, 59));
                })
                .collect(Collectors.toList());
    }
}

