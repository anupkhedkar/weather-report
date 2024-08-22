package com.weather.app.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApiKeyRateLimiterService {
    private final Map<String, ApiKeyUsage> apiKeyUsageMap = new HashMap<>();
    private final int limitPerHour = 5;

    public boolean isLimitExceeded(String apiKey) {
        ApiKeyUsage usage = apiKeyUsageMap.getOrDefault(apiKey, new ApiKeyUsage(LocalDateTime.now(), 0));
        LocalDateTime now = LocalDateTime.now();

        // Check if the current time is more than an hour past the recorded timestamp
        if (now.isAfter(usage.getTimestamp().plusHours(1))) {
            // Reset the counter and timestamp
            usage.setTimestamp(now);
            usage.setRequestCount(1);
        } else {
            // Increment the request count
            usage.incrementRequestCount();
        }

        apiKeyUsageMap.put(apiKey, usage);

        // Return true if the limit has been exceeded
        return usage.getRequestCount() > limitPerHour;
    }

    private static class ApiKeyUsage {
        private LocalDateTime timestamp;
        private int requestCount;

        public ApiKeyUsage(LocalDateTime timestamp, int requestCount) {
            this.timestamp = timestamp;
            this.requestCount = requestCount;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(int requestCount) {
            this.requestCount = requestCount;
        }

        public void incrementRequestCount() {
            this.requestCount++;
        }
    }
}

