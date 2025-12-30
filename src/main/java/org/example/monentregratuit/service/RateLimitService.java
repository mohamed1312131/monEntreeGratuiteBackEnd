package org.example.monentregratuit.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> reservationBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> newsletterBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> exposantBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> bulkEmailBuckets = new ConcurrentHashMap<>();

    public Bucket resolveReservationBucket(String key) {
        return reservationBuckets.computeIfAbsent(key, k -> createReservationBucket());
    }

    public Bucket resolveNewsletterBucket(String key) {
        return newsletterBuckets.computeIfAbsent(key, k -> createNewsletterBucket());
    }

    public Bucket resolveExposantBucket(String key) {
        return exposantBuckets.computeIfAbsent(key, k -> createExposantBucket());
    }

    public Bucket resolveBulkEmailBucket(String key) {
        return bulkEmailBuckets.computeIfAbsent(key, k -> createBulkEmailBucket());
    }

    private Bucket createReservationBucket() {
        // 3 requests per hour per IP
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createNewsletterBucket() {
        // 5 subscriptions per day per IP
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofDays(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createExposantBucket() {
        // 2 requests per day per IP
        Bandwidth limit = Bandwidth.classic(2, Refill.intervally(2, Duration.ofDays(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createBulkEmailBucket() {
        // 1 bulk email per hour (for admins)
        Bandwidth limit = Bandwidth.classic(1, Refill.intervally(1, Duration.ofHours(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public boolean allowRequest(Bucket bucket) {
        return bucket.tryConsume(1);
    }
}
