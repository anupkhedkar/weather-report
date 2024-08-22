package com.weather.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.app.repo.entities.WeatherEntity;
import com.weather.app.repo.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Autowired
    private WeatherRepository weatherRepository;


    /* @param city The name of the city for which the weather data is requested.
     * @param country The country code corresponding to the city (e.g., "uk" for the United Kingdom).
     * @param apiKey The API key required to access the OpenWeatherMap API.
     * @return A String containing the weather description retrieved from the OpenWeatherMap API.
     */
    public String getWeather(String city, String country, String apiKey) {
        return weatherRepository.findByCityAndCountry(city, country)
                .map(WeatherEntity::getDescription)
                .orElseGet(() -> {
                    String description = fetchWeatherFromApi(city, country, apiKey);
                    WeatherEntity entity = new WeatherEntity();
                    entity.setCity(city);
                    entity.setCountry(country);
                    entity.setDescription(description);
                    weatherRepository.save(entity);
                    return description;
                });

    }

    /*
     * @param city The name of the city for which the weather data is requested.
     * @param country The country code corresponding to the city (e.g., "uk" for the United Kingdom).
     * @param apiKey The API key required to access the OpenWeatherMap API.
     * @return A String containing the weather description retrieved from the OpenWeatherMap API.
     */
    private String fetchWeatherFromApi(String city, String country, String apiKey) {
        String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s,%s&APPID=%s", city, country, apiKey);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Make the API call
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // Check if the response status is 200 OK
            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse the JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());
                // Extract the weather description
                return root.at("/weather/0/description").asText();
            } else {
                // Handle the case where the API call was not successful
                throw new RuntimeException("Failed to fetch weather data from OpenWeatherMap");
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during the API call
            throw new RuntimeException("Error occurred while calling OpenWeatherMap API", e);
        }
    }

}



