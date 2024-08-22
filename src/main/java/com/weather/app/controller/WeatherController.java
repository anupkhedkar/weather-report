package com.weather.app.controller;

import com.weather.app.service.ApiKeyRateLimiterService;
import com.weather.app.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @Autowired
    private ApiKeyRateLimiterService apiKeyRateLimiterService;

    @Autowired
    private WeatherService weatherService;

    /**
     * Endpoint to get weather information based on city and country.
     *
     * @param city The name of the city for which weather is requested.
     * @param country The country code where the city is located.
     * @param apiKey The API key provided by the client in the request header.
     * @return A ResponseEntity containing the weather description or an error message.
     */
    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam String city,
                                        @RequestParam String country,
                                        @RequestParam("API-Key") String apiKey) {

        // handles the api request limit
        if (apiKeyRateLimiterService.isLimitExceeded(apiKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("API key rate limit exceeded. Try again later.");
        }

        // checks whether url contains necessary parameters
        if (city.trim().length() == 0 || country.trim().length() == 0 || apiKey.trim().length() == 0)
        {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Invalid Parameters. Please check the request");
        }
        String weatherDescription = weatherService.getWeather(city, country, apiKey);

        return ResponseEntity.ok(weatherDescription);
    }
}
